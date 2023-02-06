package in.org.iudx.adaptor.sink;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sisyphsu.dateparser.DateParserUtils;
import in.org.iudx.adaptor.codegen.*;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.process.GenericProcessFunction;
import in.org.iudx.adaptor.source.HttpSource;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;


class PgSinkTest {
    private static StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

    @BeforeAll
    public static void initialize() {
        StreamExecutionEnvironment.createLocalEnvironment();
        env.setParallelism(1);

        env.enableCheckpointing(10000L);
    }

    @Test
    void simpleTest() throws InterruptedException {
        SimpleATestTransformer trans = new SimpleATestTransformer();
        SimpleATestParser parser = new SimpleATestParser();
        SimpleADeduplicator dedup = new SimpleADeduplicator();


        ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA").setRequestType("GET").setPollingInterval(1000L);

        PostgresConfig pgConfig = new PostgresConfig();
        pgConfig.setUrl("jdbc:postgresql://localhost:15432/iudx-adaptor");
        pgConfig.setUsername("root");
        pgConfig.setPassword("adaptor@db");

        env.addSource(new HttpSource<Message>(apiConfig, parser)).keyBy((Message msg) -> msg.key).process(new GenericProcessFunction(trans, dedup)).addSink(JdbcSink.sink("insert into test (id, key, timestamp, data) values (?, ?, ?, ?)",

                (statement, msg) -> {
                    System.out.println(msg.body);
                    JSONObject data = new JSONObject(msg.body);
                    statement.setString(1, UUID.randomUUID().toString());
                    statement.setString(2, data.get("k1").toString());
                    statement.setString(3, msg.timestampString);
                    statement.setString(4, msg.body);
                }, JdbcExecutionOptions.builder().withBatchSize(1000).withBatchIntervalMs(200).withMaxRetries(5).build(), pgConfig.getBuilder()));

        try {
            env.execute("Simple Get");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void pgSinkTest() throws InterruptedException {
        SimpleATestTransformer trans = new SimpleATestTransformer();
        SimpleATestParser parser = new SimpleATestParser();
        SimpleADeduplicator dedup = new SimpleADeduplicator();

        ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA").setRequestType("GET").setPollingInterval(1000L);

        PostgresConfig pgConfig = new PostgresConfig();
        pgConfig.setUrl("jdbc:postgresql://localhost:15432/iudx-adaptor");
        pgConfig.setUsername("root");
        pgConfig.setPassword("adaptor@db");
        pgConfig.setTableName("test1");
        pgConfig.setTableSchema("{\"id\": \"string\", \"k1\": \"int\", \"time\": \"timestamp\", \"data\": \"string\"}");

        SinkFunction<Message> postgresSink = new PostgresSink().getPostgresSink(pgConfig);
        env.addSource(new HttpSource<Message>(apiConfig, parser))
                .keyBy((Message msg) -> msg.key)
                .process(new GenericProcessFunction(trans, dedup))
                .addSink(postgresSink);

        try {
            env.execute("Simple Get");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void customTest() throws InterruptedException {
        SimpleATestTransformer trans = new SimpleATestTransformer();
        SimpleATestParser parser = new SimpleATestParser();
        SimpleADeduplicator dedup = new SimpleADeduplicator();


        ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA").setRequestType("GET").setPollingInterval(1000L);

        PostgresConfig pgConfig = new PostgresConfig();
        pgConfig.setUrl("jdbc:postgresql://localhost:15432/iudx-adaptor");
        pgConfig.setUsername("root");
        pgConfig.setPassword("adaptor@db");

        String tableName = "test";

        String schemaString = "{\"id\": \"string\", \"k1\": \"int\", \"time\": \"timestamp\"}";
        JSONObject schemaObj = new JSONObject(schemaString);

        StringBuilder generateQuery = new StringBuilder("insert into " + tableName + " (");

        JSONArray columnsData = schemaObj.names();
        for (int i=0; i<columnsData.length(); i++) {
            generateQuery.append(columnsData.getString(i)).append(",");
        }
        generateQuery.deleteCharAt(generateQuery.length() - 1);
        generateQuery.append(") values (");
        generateQuery.append("?,".repeat(columnsData.length()));
        generateQuery.deleteCharAt(generateQuery.length() - 1);
        generateQuery.append(")");

        String sqlQuery = generateQuery.toString();

        env.addSource(new HttpSource<Message>(apiConfig, parser))
                .keyBy((Message msg) -> msg.key)
                .process(new GenericProcessFunction(trans, dedup))
                .addSink(JdbcSink.sink(
                        sqlQuery,
                        (statement, message) -> {
                            JSONObject data = new JSONObject(message.body);
                            data.put("id", UUID.randomUUID().toString());
                            JSONObject schema = new JSONObject(schemaString);
                            JSONArray columns = schema.names();
                            for (int i=0; i<columns.length(); i++) {
                                String column = columns.getString(i);
                                String columnType = schema.getString(column);
                                System.out.println(column);
                                switch (columnType) {
                                    case "int":
                                        statement.setInt(i + 1, data.getInt(column));
                                        break;
                                    case "string":
                                        statement.setString(i+1, data.getString(column));
                                        break;
                                    case "timestamp":
                                        LocalDateTime localDateTime = DateParserUtils.parseDateTime(data.getString("time"));
                                        statement.setTimestamp(i+1, Timestamp.valueOf(localDateTime));
                                        break;
                                }
                            }
                        },
                        JdbcExecutionOptions.builder()
                                .withBatchSize(1000)
                                .withBatchIntervalMs(200)
                                .withMaxRetries(5)
                                .build(),
                        pgConfig.getBuilder()
                ));

        try {
            env.execute("Simple Get");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
