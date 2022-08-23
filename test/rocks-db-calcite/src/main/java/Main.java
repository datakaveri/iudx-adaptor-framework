import datatypes.CustomMessage;
import datatypes.Rule;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.process.JoltTransformer;
import in.org.iudx.adaptor.process.TimeBasedDeduplicator;
import in.org.iudx.adaptor.source.HttpSource;
import in.org.iudx.adaptor.source.JsonPathParser;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSource;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
//import org.apache.flink.table.api.Expressions;
//import org.apache.flink.table.api.Slide;
//import org.apache.flink.table.api.Table;
//import org.apache.flink.table.api.TableConfig;
//import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;

import java.util.HashMap;

public class Main {

//    private static String RMQ_URI = "amqp://guest:guest@rmq:5672";
    private static String RMQ_URI = "amqp://guest:guest@localhost:5672";


    private static String RMQ_SQL_QUEUE_NAME = "rules";

    public static void main(String[] args) {
        HashMap<String, String> propertyMap = new HashMap<>();
        propertyMap.put("appName", "test");
        ParameterTool parameters = ParameterTool.fromMap(propertyMap);
//        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
        env.enableCheckpointing(1000 * 100 * 1000);

        // Table Env
//        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
//        TableConfig tableConfig = tableEnv.getConfig();
//        // set low-level key-value options
//        tableConfig.set("table.exec.mini-batch.enabled", "true");
//        tableConfig.set("table.exec.mini-batch.allow-latency", "5 s");
//        tableConfig.set("table.exec.mini-batch.size", "5000");


        // RMQ Source for SQL Queries
        final RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder().setUri(RMQ_URI).build();
        final DataStream<String> sqlSourceStream = env.addSource(new RMQSource<String>(connectionConfig, RMQ_SQL_QUEUE_NAME, new SimpleStringSchema())).setParallelism(1);

        DataStream<Rule> rules = sqlSourceStream.flatMap(new RuleSourceDeSerializer()).uid("RuleSource").name("Rules Source").setParallelism(1);


        BroadcastStream<Rule> ruleBroadcastStream = rules.broadcast(Descriptors.ruleMapStateDescriptor);


        // Http Source for data

//        ApiConfig apiConfig = new ApiConfig().setUrl("http://13.232.120.105:30002/simpleA").setRequestType("GET").setPollingInterval(1000);
        ApiConfig apiConfig = new ApiConfig().setUrl("http://localhost:8888/simpleA").setRequestType("GET").setPollingInterval(1000);
        String parseSpec = "{\"messageContainer\":\"single\",\"timestampPath\":\"$.time\",\"keyPath\":\"$.deviceId\",\"type\":\"json\"}";
        JsonPathParser<Message> parser = new JsonPathParser<>(parseSpec);
        DataStreamSource<Message> so = env.addSource(new HttpSource<>(apiConfig, parser));
        TimeBasedDeduplicator dedup = new TimeBasedDeduplicator();
        String transformSpec = "{\"type\":\"jolt\",\"joltSpec\":[{\"operation\":\"shift\",\"spec\":{\"k1\":\"k1\",\"time\":\"observationDateTime\",\"deviceId\":\"id\"}},{\"operation\":\"modify-overwrite-beta\",\"spec\":{\"id\":\"=concat('datakaveri.org/123/', id)\"}}]}";
        JoltTransformer trans = new JoltTransformer(transformSpec);


        SingleOutputStreamOperator<String> out = so
                .keyBy((Message msg) -> msg.key)
//                .process(new GenericProcessFunction(trans, dedup))
//                .uid("GenericProcessFunction")
//                .name("Generic Process Function")
//                .keyBy((Message msg) -> msg.key)
                .connect(ruleBroadcastStream)
                .process(new RuleFunction())
                .uid("RuleSourceFunction")
                .name("Rule Source Function")
//                .assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps())
//                .keyBy((Keyed::getKey))
//                .connect(ruleBroadcastStream)
//                .process(new RuleExecutorFunction())
//                .uid("RuleExecutorFunction")
//                .name("Rule Executor Function")
                .setParallelism(1);


//        Table inputTable = tableEnv.fromDataStream(
//                out,
//                Expressions.$("body"),
//                Expressions.$("key"),
//                Expressions.$("timestamp"),
//                Expressions.$("timestampString"),
//                Expressions.$("ruleString"),
//                Expressions.$("sqlTimestamp").rowtime()
//        ).as("data", "key", "timestamp", "timestampString");
//
//        Table tableResult = inputTable
//                .window(Slide
//                        .over(Expressions.lit(30).seconds())
//                        .every(Expressions.lit(10).seconds())
//                        .on(Expressions.$("sqlTimestamp"))
//                        .as("w"))
//                .groupBy(Expressions.$("w"))
//                .select(Expressions.$("*"));

//
//        tableEnv.createTemporaryView("InputTable", tableResult);
//        Table resultTable = tableEnv.sqlQuery("SELECT * FROM InputTable");

//        DataStream<Row> resultStream = tableEnv.toDataStream(tableResult);
//        resultStream.addSink(new RMQSink<Row>(connectionConfig, "sink", new SerializationSchema<Row>() {
//            @Override
//            public byte[] serialize(Row row) {
//                System.out.println(row);
//                return row.toString().getBytes();
//            }
//        })).uid("PrintSink").name("Print Sink").setParallelism(1);

        out.addSink(new RMQSink<>(connectionConfig, "sink", new SimpleStringSchema())).uid("RmqSink").name("RMQ Sink").setParallelism(1);

        try {
            env.getConfig().setGlobalJobParameters(parameters);
//            System.out.println("-------------------------------------------");
//            System.out.println(env.getExecutionPlan());
//            System.out.println("-------------------------------------------");
            env.execute();
        } catch (Exception e) {
        }
    }

    public static class Descriptors {
        public static final MapStateDescriptor<String, Rule> ruleMapStateDescriptor = new MapStateDescriptor<>("RulesBroadcastState", BasicTypeInfo.STRING_TYPE_INFO, TypeInformation.of(Rule.class));
    }
}
