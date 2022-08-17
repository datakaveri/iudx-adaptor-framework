import datatypes.CustomMessage;
import datatypes.Rule;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.common.state.*;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import sql.CustomSchema;

import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


public class RuleFunction extends KeyedBroadcastProcessFunction<String, Message, Rule, String> {

    private transient ListState<Message> listState;

    private final ListStateDescriptor<Message> listStateDescriptor = new ListStateDescriptor<>("listState", TypeInformation.of(new TypeHint<>() {
    }));

    private transient CalciteConnection calciteConnection;
    @Override
    public void open(Configuration parameters) throws ClassNotFoundException, SQLException {
        listState = getRuntimeContext().getListState(listStateDescriptor);

        Class.forName("org.apache.calcite.jdbc.Driver");

        Properties info = new Properties();
        info.setProperty("lex", "JAVA");
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);

        calciteConnection = connection.unwrap(CalciteConnection.class);

    }

//    @Override
//    public void processElement(Message message, KeyedBroadcastProcessFunction<String, Message, Rule, CustomMessage>.ReadOnlyContext readOnlyContext, Collector<CustomMessage> collector) throws Exception {
//        ReadOnlyBroadcastState<String, Rule> ruleState = readOnlyContext.getBroadcastState(Main.Descriptors.ruleMapStateDescriptor);
////        Tuple2<String, Message> data = new Tuple2<>(message.timestampString, message);
//        listState.add(message);
//        SchemaPlus rootSchema = calciteConnection.getRootSchema();
//
//
//        Iterator<Message> iterator = listState.get().iterator();
//        List<Message> list = IteratorUtils.toList(iterator);
//
//        CustomSchema schema = new CustomSchema();
//
//        schema.state = list;
//        rootSchema.add("listState", schema);
//
//        Statement statement = calciteConnection.createStatement();
//        String sql = "select key from listState.Message";
//        ResultSet rs = statement.executeQuery(sql);
//
//        while (rs.next()) {
//            String key = rs.getString("key");
//            collector.collect(key);
//        }
//
////        for (Object ruleObject : ruleState.immutableEntries()) {
////            final Rule rule = new Rule(ruleObject.toString());
////            CustomMessage customMessage = new CustomMessage();
////            customMessage.setRuleString(rule.sqlQuery);
////            customMessage.setSqlTimestamp(message.timestamp);
////            customMessage.setResponseBody(message.body);
////            customMessage.setKey(message.key);
////            customMessage.setEventTimestamp(message.timestamp);
////            customMessage.setEventTimeAsString(message.timestampString);
////            collector.collect(customMessage);
////        }
//    }

//    @Override
//    public void processBroadcastElement(Rule rule, KeyedBroadcastProcessFunction<String, Message, Rule, CustomMessage>.Context context, Collector<CustomMessage> collector) throws Exception {
//        BroadcastState<String, Rule> broadcastState = context.getBroadcastState(Main.Descriptors.ruleMapStateDescriptor);
//        broadcastState.put(rule.sqlQuery, rule);
//    }

    @Override
    public void processElement(Message message, KeyedBroadcastProcessFunction<String, Message, Rule, String>.ReadOnlyContext readOnlyContext, Collector<String> collector) throws Exception {
        ReadOnlyBroadcastState<String, Rule> ruleState = readOnlyContext.getBroadcastState(Main.Descriptors.ruleMapStateDescriptor);
//        Tuple2<String, Message> data = new Tuple2<>(message.timestampString, message);
        listState.add(message);
        SchemaPlus rootSchema = calciteConnection.getRootSchema();


        Iterator<Message> iterator = listState.get().iterator();
        List<Message> list = IteratorUtils.toList(iterator);

        CustomSchema schema = new CustomSchema();

        schema.state = list;
        rootSchema.add("listState", schema);

        Statement statement = calciteConnection.createStatement();
        String sql = "select key from listState.Message";
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            String key = rs.getString("key");
            collector.collect(key);
        }
//        collector.collect(list.get(0).key);
    }

    @Override
    public void processBroadcastElement(Rule rule, KeyedBroadcastProcessFunction<String, Message, Rule, String>.Context context, Collector<String> collector) throws Exception {
        BroadcastState<String, Rule> broadcastState = context.getBroadcastState(Main.Descriptors.ruleMapStateDescriptor);
        broadcastState.put(rule.sqlQuery, rule);
    }
}
