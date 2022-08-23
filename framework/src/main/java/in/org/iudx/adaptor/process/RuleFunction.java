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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    @Override
    public void processElement(Message message, KeyedBroadcastProcessFunction<String, Message, Rule, String>.ReadOnlyContext readOnlyContext, Collector<String> collector) throws Exception {
        ReadOnlyBroadcastState<String, Rule> ruleState = readOnlyContext.getBroadcastState(RuleStateDescriptor.ruleMapStateDescriptor);
        listState.add(message);

        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Iterator<Message> iterator = listState.get().iterator();

        List<Message> list = IteratorUtils.toList(iterator);

        Schema schema = new Schema();

        // TODO
//        schema.setData(list);
        rootSchema.add("listState", schema);

        try (Statement statement = calciteConnection.createStatement()) {
            String sql = "select * from listState.state";
            ResultSet rs = statement.executeQuery(sql);

            while (rs.next()) {
                String result = rs.getString("deviceId");
                collector.collect(String.valueOf(result));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void processBroadcastElement(Rule rule, KeyedBroadcastProcessFunction<String, Message, Rule, String>.Context context, Collector<String> collector) throws Exception {
        BroadcastState<String, Rule> broadcastState = context.getBroadcastState(RuleStateDescriptor.ruleMapStateDescriptor);
        broadcastState.put(rule.sqlQuery, rule);
    }
}
