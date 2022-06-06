package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.MinioConfig;
import in.org.iudx.adaptor.codegen.SimpleADeduplicator;
import in.org.iudx.adaptor.codegen.SimpleATestParser;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.source.HttpSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BoundedProcessTest {

    public static MiniClusterWithClientResource flinkCluster;

    private static final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

    private static final Logger LOGGER = LogManager.getLogger(BoundedProcessTest.class);


    @BeforeAll
    public static void initialize() {
        LOGGER.debug("Info: Testing");
        flinkCluster =
                new MiniClusterWithClientResource(
                        new MiniClusterResourceConfiguration.Builder()
                                .setNumberSlotsPerTaskManager(2)
                                .setNumberTaskManagers(1)
                                .build());

        StreamExecutionEnvironment.createLocalEnvironment();
        env.setParallelism(1);
    }

    @Test
    void testBoundedProcess() throws InterruptedException {

        SimpleATestParser parser = new SimpleATestParser();
        SimpleADeduplicator dedup = new SimpleADeduplicator();

        ApiConfig apiConfig =
                new ApiConfig().setUrl("http://127.0.0.1:8888/constantA")
                        .setRequestType("GET")
                        .setHeader("Authorization",
                                "Basic YWRtaW46YWRtaW4=")
                        .setPollingInterval(-1);

        MinioConfig minioConfig =
                new MinioConfig.Builder("http://localhost:9000")
                        .bucket("custom-state")
                        .object("testState")
                        .credentials("minio", "minio123")
                        .build();

        String  script = "function main(obj) { var out = {}; var inp = JSON.parse(obj); out[\"id\"] = \"datakaveri.org/a/b\" + inp[\"id\"]; out[\"k1\"] = inp[\"k1\"]; out[\"observationDateTime\"] = inp[\"time\"]; return JSON.stringify(out); }";
        String transformSpec = new JSONObject().put("script", script).toString();

        env.addSource(new HttpSource<>(apiConfig, parser))
                .keyBy((Message msg) -> msg.key)
                .process(new BoundedProcessFunction(dedup, minioConfig))
                .flatMap(new JSProcessFunction(transformSpec))
                .addSink(new DumbSink());

        try {
            env.execute("bounded process test");
        } catch (Exception e) {
            LOGGER.error(e);
        }

    }

}
