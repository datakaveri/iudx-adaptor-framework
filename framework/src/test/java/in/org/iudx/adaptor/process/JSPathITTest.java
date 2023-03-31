package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.source.RMQGenericSource;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JSPathITTest {
    private static final Logger LOGGER = LogManager.getLogger(RuleFunctionITTest.class);
    private static StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();


    @BeforeAll
    public static void initialize() {
        LOGGER.debug("Info: Testing");
        StreamExecutionEnvironment.createLocalEnvironment();
        env.setParallelism(1);
        env.enableCheckpointing(10000L);
    }


    @Test
    void testJS() throws Exception {
        RMQConfig config = new RMQConfig();
        config.setUri("amqp://guest:guest@localhost:5672");
        config.setQueueName("test");
        config.setRoutingKey("routingKey");
        RMQGenericSource source = new RMQGenericSource<>(config, TypeInformation.of(Message.class), "surat-itms-pg-data-onboard", "{\"messageContainer\":\"single\",\"timestampPath\":\"$.observationDateTime\",\"keyPath\":\"$.id\",\"type\":\"json\"}");
        DataStreamSource<Message> so = env.addSource(source);
        TimeBasedDeduplicator dedup = new TimeBasedDeduplicator();
        String transformSpec = "{\"template\":\"{'id': 'suratmunicipal.org/6db486cb4f720e8585ba1f45a931c63c25dbbbda/rs.iudx.org.in/surat-itms-realtime-info/surat-itms-live-eta'," +
                "'actual_trip_start_time':'123','observationDateTime':'123','last_stop_arrival_time':'123','trip_id': '676'," +
                "'route_id':'123','trip_direction':'123', 'vehicle_label': 'xxxyy','license_plate': 'xxxyy'," +
                "'last_stop_id':'abc', 'location.type': 'Point', 'location.coordinates': [1,1], 'speed': -1, 'trip_delay': -1}\"," +
                "\"jsonPathSpec\":[{\"inputValuePath\":\"$.id\",\"outputKeyPath\":\"$.id\"}," +
                "{\"inputValuePath\":\"$.trip_id\",\"valueModifierScript\":\"parseInt(value, 10)\",\"outputKeyPath\":\"$.trip_id\"}," +
                "{\"inputValuePath\":\"$.route_id\",\"outputKeyPath\":\"$.route_id\"}," +
                "{\"inputValuePath\":\"$.trip_direction\",\"outputKeyPath\":\"$.trip_direction\"}," +
                "{\"inputValuePath\":\"$.actual_trip_start_time\",\"outputKeyPath\":\"$.actual_trip_start_time\"}," +
                "{\"inputValuePath\":\"$.last_stop_arrival_time\",\"outputKeyPath\":\"$.last_stop_arrival_time\"}," +
                "{\"inputValuePath\":\"$.vehicle_label\",\"outputKeyPath\":\"$.vehicle_label\"}," +
                "{\"inputValuePath\":\"$.license_plate\",\"outputKeyPath\":\"$.license_plate\"}," +
                "{\"inputValuePath\":\"$.last_stop_id\",\"valueModifierScript\":\"parseInt(value, 10)\",\"outputKeyPath\":\"$.last_stop_id\"}," +
                "{\"inputValuePath\":\"$.location.type\",\"outputKeyPath\":\"$['location.type']\"}," +
                "{\"inputValuePath\":\"$.location.coordinates\",\"outputKeyPath\":\"$['location.coordinates']\"}," +
//                "{\"inputValuePath\":\"$.location.coordinates\",\"valueModifierScript\":\"String(value)\",\"outputKeyPath\":\"$['location.coordinates']\"}," +
                "{\"inputValuePath\":\"$.speed\",\"valueModifierScript\":\"parseFloat(value)\",\"outputKeyPath\":\"$.speed\"}," +
                "{\"inputValuePath\":\"$.observationDateTime\",\"outputKeyPath\":\"$.observationDateTime\"}," +
                "{\"inputValuePath\":\"$.trip_delay\",\"valueModifierScript\":\"parseFloat(value)\",\"outputKeyPath\":\"$.trip_delay\"}]," +
                "\"type\":\"jsPath\"}";

        SingleOutputStreamOperator<Message> ds = so.keyBy((Message msg) -> msg.key).process(new GenericProcessFunction(dedup)).flatMap(new JSPathProcessFunction(transformSpec));

        ds.addSink(new DumbSink());

        CompletableFuture<Void> handle = CompletableFuture.runAsync(() -> {
            try {
                env.execute("Simple Get");
            } catch (Exception e) {
                System.out.println(e);
            }
        });
        try {
            // increased time to 4 minutes to test timer
            handle.get(4, TimeUnit.MINUTES);
        } catch (TimeoutException | ExecutionException e) {
            handle.cancel(true); // this will interrupt the job execution thread, cancel and close the job
        }
    }
}
