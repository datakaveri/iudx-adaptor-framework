import datatypes.CustomMessage;
import datatypes.Rule;
import in.org.iudx.adaptor.datatypes.Message;
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
import sql.CustomSchema;
import tech.tablesaw.api.Table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
        ReadOnlyBroadcastState<String, Rule> ruleState = readOnlyContext.getBroadcastState(Main.Descriptors.ruleMapStateDescriptor);
        listState.add(message);

        SchemaPlus rootSchema = calciteConnection.getRootSchema();
        Iterator<Message> iterator = listState.get().iterator();

        List<Message> list = IteratorUtils.toList(iterator);

        CustomSchema schema = new CustomSchema();

        schema.setData(list);
        rootSchema.add("listState", schema);

        try (Statement statement = calciteConnection.createStatement()) {
            String sql = "select count(*) as countData from listState.state";
            ResultSet rs = statement.executeQuery(sql);

//            System.out.println(Table.read().db(rs).print());
            while (rs.next()) {
                int result = rs.getInt("countData");
                System.out.println("====================================");
                System.out.println(result);
                collector.collect(String.valueOf(result));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
//        collector.collect(list.get(0).key);
    }

    @Override
    public void processBroadcastElement(Rule rule, KeyedBroadcastProcessFunction<String, Message, Rule, String>.Context context, Collector<String> collector) throws Exception {
        BroadcastState<String, Rule> broadcastState = context.getBroadcastState(Main.Descriptors.ruleMapStateDescriptor);
        broadcastState.put(rule.sqlQuery, rule);
    }
}
