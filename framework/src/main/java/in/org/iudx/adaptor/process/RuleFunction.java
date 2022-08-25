package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;
import in.org.iudx.adaptor.sql.Schema;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


import in.org.iudx.adaptor.utils.JsonFlatten;

public class RuleFunction extends KeyedBroadcastProcessFunction<String, Message, Rule, String> {

    private transient ListState<LinkedHashMap<String,Object>> listState;
    private final ListStateDescriptor<LinkedHashMap<String,Object>> listStateDescriptor 
        = new ListStateDescriptor<>("listState",
                                    TypeInformation.of(
                                      new TypeHint<LinkedHashMap<String,Object>>() {
    }));

    private transient CalciteConnection calciteConnection;

    private static int EXPIRY_TIME = Integer.MIN_VALUE;

    @Override
    public void open(Configuration parameters) throws ClassNotFoundException, SQLException {


        Properties info = new Properties();
        info.setProperty("lex", "JAVA");
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
        calciteConnection = connection.unwrap(CalciteConnection.class);

        listState = getRuntimeContext().getListState(listStateDescriptor);

    }

    @Override
    public void processElement(Message message, KeyedBroadcastProcessFunction<String,
                  Message, Rule, String>.ReadOnlyContext readOnlyContext,
                  Collector<String> collector) throws Exception {

        ReadOnlyBroadcastState<Integer, Rule> ruleState
              = readOnlyContext.getBroadcastState(RuleStateDescriptor.ruleMapStateDescriptor);

        listState.add(
            (LinkedHashMap<String,Object>) new JsonFlatten(new ObjectMapper()
                                                              .readTree(message.toString()))
                                                          .flatten());

        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Iterator<LinkedHashMap<String,Object>> iterator = listState.get().iterator();
        List<LinkedHashMap<String, Object>> list = IteratorUtils.toList(iterator);

        Schema schema = new Schema();
        schema.setData(list);
        rootSchema.add("listState", schema);

        try (Statement statement = calciteConnection.createStatement()) {
            String sql = "select * from listState.state";
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                String result = rs.getString("index");
                collector.collect(String.valueOf(result));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void processBroadcastElement(Rule rule, KeyedBroadcastProcessFunction<String, Message, Rule, String>.Context context, Collector<String> collector) throws Exception {
        BroadcastState<Integer, Rule> broadcastState = context.getBroadcastState(RuleStateDescriptor.ruleMapStateDescriptor);
        broadcastState.put(rule.ruleId, rule);

        updateExpiryTime(rule, broadcastState);
    }


    @Override
    public void onTimer(final long timestamp, final OnTimerContext ctx, final Collector<String> collector) throws Exception {

        Rule rule = ctx.getBroadcastState(RuleStateDescriptor.ruleMapStateDescriptor).get(EXPIRY_TIME);

        Optional<Long> cleanupEventTimeWindow = Optional.ofNullable(rule).map(Rule::getWindowMillis);
        Optional<Long> cleanupEventTimeThreshold = cleanupEventTimeWindow.map(window -> timestamp - window);

        cleanupEventTimeThreshold.ifPresent(this::removeElementFromState);
    }

    private void updateExpiryTime(Rule rule, BroadcastState<Integer, Rule> broadcastState)
            throws Exception {
        Rule widestWindowRule = broadcastState.get(EXPIRY_TIME);


        if (widestWindowRule == null) {
            broadcastState.put(EXPIRY_TIME, rule);
            return;
        }

        if (widestWindowRule.getWindowMillis() < rule.getWindowMillis()) {
            broadcastState.put(EXPIRY_TIME, rule);
        }
    }

    private void removeElementFromState(Long threshold) {
        try {
            Iterator<LinkedHashMap<String, Object>> iterator = listState.get().iterator();
            while (iterator.hasNext()) {
                LinkedHashMap<String, Object> obj = iterator.next();
                long eventTime = (long) obj.get("observationDateTime");
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
