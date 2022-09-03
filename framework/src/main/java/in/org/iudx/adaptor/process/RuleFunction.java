package in.org.iudx.adaptor.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;
import in.org.iudx.adaptor.sql.Schema;
import in.org.iudx.adaptor.utils.JsonFlatten;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

public class RuleFunction extends KeyedBroadcastProcessFunction<String, Message, Rule, RuleResult> {

  private int EXPIRY_TIME_RULE_ID = Integer.MIN_VALUE;
  private ListStateDescriptor<LinkedHashMap<String, Object>> listStateDescriptor;

  private ListState<LinkedHashMap<String, Object>> listState;
  private transient CalciteConnection calciteConnection;

  @Override
  public void open(Configuration parameters) throws ClassNotFoundException, SQLException {

    Properties info = new Properties();
    info.setProperty("lex", "JAVA");
    Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
    calciteConnection = connection.unwrap(CalciteConnection.class);


    listStateDescriptor = new ListStateDescriptor<>("listState",
            TypeInformation.of(new TypeHint<LinkedHashMap<String, Object>>() {
            }));
    listState = getRuntimeContext().getListState(listStateDescriptor);
  }

  @Override
  public void processElement(Message message,
                             KeyedBroadcastProcessFunction<String, Message, Rule, RuleResult>.ReadOnlyContext readOnlyContext,
                             Collector<RuleResult> collector) throws Exception {
    ReadOnlyBroadcastState<Integer, Rule> ruleState = readOnlyContext.getBroadcastState(
            RuleStateDescriptor.ruleMapStateDescriptor);

    SchemaPlus rootSchema = calciteConnection.getRootSchema();

    LinkedHashMap<String, Object> obj = new JsonFlatten(
            new ObjectMapper().readTree(message.toString())).flatten();

    listState.add(obj);

    long cleanupTime = readOnlyContext.timestamp();

    readOnlyContext.timerService().registerEventTimeTimer(cleanupTime);

    Iterator<LinkedHashMap<String, Object>> iterator = listState.get().iterator();
    List<LinkedHashMap<String, Object>> list = IteratorUtils.toList(iterator);

    System.out.println(list);
    Schema schema = new Schema();
    schema.setData(list);
    rootSchema.add("listState", schema);

    ruleState.immutableEntries().forEach(rule -> {
      try (Statement statement = calciteConnection.createStatement()) {
        String q = rule.getValue().sqlQuery;
        q = q.replace("TABLE", "listState.state");
        ResultSet rs = statement.executeQuery(q);
        JSONArray json = new JSONArray();
        while (rs.next()) {
          int columnCount = rs.getMetaData().getColumnCount();
          JSONObject jsonObject = new JSONObject();
          for (int i = 1; i <= columnCount; i++) {
            String column = rs.getMetaData().getColumnName(i);
            jsonObject.put(column, rs.getObject(column));
          }
          json.put(jsonObject);
        }
        RuleResult ruleResult = new RuleResult(json.toString(), rule.getValue().sinkExchangeKey,
                rule.getValue().sinkRoutingKey);
        collector.collect(ruleResult);
      } catch (Exception e) {
        System.out.println(e);
      }
    });
  }

  @Override
  public void processBroadcastElement(Rule rule,
                                      KeyedBroadcastProcessFunction<String, Message, Rule,
                                              RuleResult>.Context context,
                                      Collector<RuleResult> collector) throws Exception {
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
          throw new RuntimeException(e);
        }
      });
    }
  }

  @Override
  public void onTimer(final long timestamp, final OnTimerContext ctx,
                      final Collector<RuleResult> collector) throws Exception {
    Rule rule = ctx.getBroadcastState(RuleStateDescriptor.ruleMapStateDescriptor)
            .get(EXPIRY_TIME_RULE_ID);

    Optional<Long> cleanupEventTimeWindow = Optional.ofNullable(rule).map(Rule::getWindowMillis);
    Optional<Long> cleanupEventTimeThreshold = cleanupEventTimeWindow.map(
            window -> timestamp - window);

    cleanupEventTimeThreshold.ifPresent(this::removeElementFromState);
  }

  private void updateExpiryTime(Rule rule, BroadcastState<Integer, Rule> broadcastState)
          throws Exception {
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
    try {
      Iterator<LinkedHashMap<String, Object>> iterator = listState.get().iterator();
      while (iterator.hasNext()) {
        LinkedHashMap<String, Object> obj = iterator.next();
        Timestamp eventTimestamp = (Timestamp) obj.get("observationDateTime");
        Long eventTime = eventTimestamp.toInstant().toEpochMilli();
        if (eventTime < threshold) {
          iterator.remove();
        }
      }
      List<LinkedHashMap<String, Object>> list = IteratorUtils.toList(iterator);
      listState.update(list);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
