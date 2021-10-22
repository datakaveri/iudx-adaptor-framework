package in.org.iudx.adaptor.utils;

import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleATestParser;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.source.HttpSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

public class HashMapStateTest {

    public static MiniClusterWithClientResource flinkCluster;

    private static final StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

    private static final Logger LOGGER = LogManager.getLogger(HashMapStateTest.class);

    @BeforeAll
    public static void initialize() {
        LOGGER.debug("Info: Testing");
        flinkCluster = new MiniClusterWithClientResource(
                        new MiniClusterResourceConfiguration.Builder()
                                .setNumberSlotsPerTaskManager(2)
                                .setNumberTaskManagers(1)
                                .build());

        StreamExecutionEnvironment.createLocalEnvironment();
        env.setParallelism(1);
    }

    @Test
    void simpleTest() {
        SimpleATestParser parser = new SimpleATestParser();

        ApiConfig apiConfig =
                new ApiConfig().setUrl("http://127.0.0.1:8888/auth/simpleA")
                        .setRequestType("GET")
                        .setHeader("Authorization",
                                "Basic YWRtaW46YWRtaW4=")
                        .setPollingInterval(-1);

        DataStreamSource<Message> so = env.addSource(new HttpSource<>(apiConfig, parser));

        /* Include process */
        so.process(new ProcessFunction<Message, Message>() {
                private HashMapState hashMapState;

                @Override
                public void open(Configuration config) {
                    hashMapState = new HashMapState();
                }

                @Override
                public void processElement(
                        Message message,
                        Context context,
                        Collector<Message> collector) throws Exception {

                    hashMapState.addMessage(message);
                    LOGGER.info(" Updated state ");

                    byte[] byteStream = hashMapState.serialize();
                    FileOutputStream fileOutputStream = new FileOutputStream("./hashmapstate");
                    fileOutputStream.write(byteStream);
                    LOGGER.info(" Serialized state ");

                    File file = new File("./hashmapstate");
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] fileContent = new byte[(int) file.length()];
                    fileInputStream.read(fileContent);

                    HashMap<?, ?> result = hashMapState.deserialize(fileContent);
                    LOGGER.info(" Deserialized state -> " + result);

                    hashMapState.removeMessage(message);

                    // Delte the file
                    file.delete();
                }

            });

        try {
            env.execute("Hash Map state test");
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
}
