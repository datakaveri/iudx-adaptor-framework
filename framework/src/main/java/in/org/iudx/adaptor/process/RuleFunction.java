package in.org.iudx.adaptor.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;
import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.sql.Schema;
import in.org.iudx.adaptor.utils.JsonFlatten;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

public class RuleFunction extends KeyedBroadcastProcessFunction<String, Message, Rule, RuleResult> {

  private int EXPIRY_TIME_RULE_ID = Integer.MIN_VALUE;

  private ListState<LinkedHashMap<String, Object>> listState;
  private transient CalciteConnection calciteConnection;

  private boolean enabledProcessTimer = true;

  transient CustomLogger logger;
  private transient Counter elementCounter;
  private transient Counter alertCounter;
  private transient Counter ruleCounter;

  @Override
  public void open(Configuration parameters) throws ClassNotFoundException, SQLException {
    ExecutionConfig.GlobalJobParameters params = getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
    String appName = params.toMap().get("appName");
    logger = new CustomLogger(JSPathProcessFunction.class, appName);
    this.elementCounter = getRuntimeContext()
            .getMetricGroup()
            .counter("RuleFunctionElementCounter");

    this.alertCounter = getRuntimeContext()
            .getMetricGroup()
            .counter("RuleFunctionAlertCounter");

    this.ruleCounter = getRuntimeContext()
            .getMetricGroup()
            .counter("RuleFunctionRuleCounter");

    Properties info = new Properties();
    info.setProperty("lex", "JAVA");
    Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
    calciteConnection = connection.unwrap(CalciteConnection.class);

    ListStateDescriptor<LinkedHashMap<String, Object>> listStateDescriptor =
            new ListStateDescriptor<>(
            "listState",
            TypeInformation.of(new TypeHint<LinkedHashMap<String, Object>>() {
            }));
    listState = getRuntimeContext().getListState(listStateDescriptor);
  }

  @Override
  public void processElement(Message message,
                             KeyedBroadcastProcessFunction<String, Message, Rule, RuleResult>.ReadOnlyContext readOnlyContext,
                             Collector<RuleResult> collector) throws Exception {
    this.elementCounter.inc();
    logger.debug("[event_key - " + message.key + "] Processing rule event");
    ReadOnlyBroadcastState<Integer, Rule> ruleState = readOnlyContext.getBroadcastState(
            RuleStateDescriptor.ruleMapStateDescriptor);

    SchemaPlus rootSchema = calciteConnection.getRootSchema();

    LinkedHashMap<String, Object> obj = new JsonFlatten(
            new ObjectMapper().readTree(message.toString())).flatten();

    listState.add(obj);

    long cleanupTime = getCleanupTime(ruleState, readOnlyContext, message);

    if (enabledProcessTimer) {
      readOnlyContext.timerService().registerProcessingTimeTimer(cleanupTime);
      enabledProcessTimer = false;
    }

    Iterator<LinkedHashMap<String, Object>> iterator = listState.get().iterator();
    List<LinkedHashMap<String, Object>> list = IteratorUtils.toList(iterator);

    Schema schema = new Schema();
    schema.setData(list);
    rootSchema.add("listState", schema);

    ruleState.immutableEntries().forEach(rule -> {
      try (Statement statement = calciteConnection.createStatement()) {
        String q = rule.getValue().sqlQuery;
        q = q.replace("TABLE", "listState.state");
        q = q.replace("''", "'");
        ResultSet rs = statement.executeQuery(q);
        JSONArray json = new JSONArray();
        while (rs.next()) {
          int columnCount = rs.getMetaData().getColumnCount();
          JSONObject jsonObject = new JSONObject();
          for (int i = 1; i <= columnCount; i++) {
            String column = rs.getMetaData().getColumnName(i);
            jsonObject.put(column, rs.getObject(column));
          }
          if (jsonObject.length() > 0) {
            json.put(jsonObject);
          }
        }
        if (json.length() > 0) {
          RuleResult ruleResult = new RuleResult(json.toString(), rule.getValue().sinkExchangeKey,
                  rule.getValue().sinkRoutingKey);
          collector.collect(ruleResult);
          this.alertCounter.inc();
        }
      } catch (Exception e) {
        logger.error("Error in executing query", e);
      }
    });
  }

  @Override
  public void processBroadcastElement(Rule rule,
                                      KeyedBroadcastProcessFunction<String, Message, Rule,
                                              RuleResult>.Context context,
                                      Collector<RuleResult> collector) throws Exception {
    this.ruleCounter.inc();
    logger.debug("[rule_id - " + rule.ruleId + "] Processing rule broadcast");
    BroadcastState<Integer, Rule> broadcastState = context.getBroadcastState(
            RuleStateDescriptor.ruleMapStateDescriptor);
    if (rule.type == Rule.RuleType.RULE) {
      broadcastState.put(rule.ruleId, rule);
      updateExpiryTime(rule, broadcastState);
    } else if (rule.type == Rule.RuleType.DELETE) {
      broadcastState.remove(rule.ruleId);
      if (rule.ruleId == EXPIRY_TIME_RULE_ID) {
        EXPIRY_TIME_RULE_ID = Integer.MIN_VALUE;
      }
      broadcastState.immutableEntries().forEach(ruleEntry -> {
        try {
          updateExpiryTime(rule, broadcastState);
        } catch (Exception e) {
          logger.error("Broadcast process error", e);
          throw new RuntimeException(e);
        }
      });
    }
  }

  @Override
  public void onTimer(final long timestamp, final OnTimerContext ctx,
                      final Collector<RuleResult> collector) throws Exception {
    logger.debug("Running timer function for cleanup");
    Rule rule = ctx.getBroadcastState(RuleStateDescriptor.ruleMapStateDescriptor)
            .get(EXPIRY_TIME_RULE_ID);

    Optional<Long> cleanupEventTimeWindow = Optional.ofNullable(rule).map(Rule::getWindowMillis);
    Optional<Long> cleanupEventTimeThreshold = cleanupEventTimeWindow.map(
            window -> timestamp - window);

    cleanupEventTimeThreshold.ifPresent(this::removeElementFromState);
  }

  private long getCleanupTime(ReadOnlyBroadcastState<Integer, Rule> ruleState,
                              ReadOnlyContext ctx, Message msg) throws Exception {
    Rule rule = ruleState.get(EXPIRY_TIME_RULE_ID);

    if (rule == null) {
      return ctx.timestamp() + msg.getExpiry();
    }

    return ctx.timestamp() + rule.getWindowMillis();
  }

  private void updateExpiryTime(Rule rule, BroadcastState<Integer, Rule> broadcastState)
          throws Exception {
    logger.debug("Update widest expiry time");
    Rule widestWindowRule = broadcastState.get(EXPIRY_TIME_RULE_ID);

    if (widestWindowRule == null) {
      broadcastState.put(rule.ruleId, rule);
      EXPIRY_TIME_RULE_ID = rule.ruleId;
      return;
    }

    if (widestWindowRule.getWindowMillis() < rule.getWindowMillis()) {
      broadcastState.put(rule.ruleId, rule);
      EXPIRY_TIME_RULE_ID = rule.ruleId;
    }
  }

  private void removeElementFromState(Long threshold) {
    enabledProcessTimer = true;
    logger.debug("Cleanup expired list state elements");
    try {
      Iterator<LinkedHashMap<String, Object>> iterator = listState.get().iterator();
      while (iterator.hasNext()) {
        LinkedHashMap<String, Object> obj = iterator.next();
        Timestamp eventTimestamp = (Timestamp) obj.get("observationDateTime");
        long eventTime = eventTimestamp.toInstant().toEpochMilli();
        if (eventTime < threshold) {
          iterator.remove();
        }
      }
      List<LinkedHashMap<String, Object>> list = IteratorUtils.toList(iterator);
      listState.update(list);
    } catch (Exception e) {
      logger.error("Error in removing element from state", e);
      throw new RuntimeException(e);
    }
  }
}
