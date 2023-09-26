package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.source.JsonPathParser;
import in.org.iudx.adaptor.source.RMQGenericSource;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JSITTest {

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
        config.setUri("amqp://localhost:24567/");
        config.setQueueName("queue");
        config.setRoutingKey("route-key");

        JsonPathParser<Message> parser = new JsonPathParser<>("{\"messageContainer\":\"single\",\"timestampPath\":\"$.observationDateTime\",\"keyPath\":\"$.id\",\"type\":\"json\"}");

        RMQGenericSource source = new RMQGenericSource<>(config, TypeInformation.of(Message.class), "data", parser);
        DataStreamSource<Message> so = env.addSource(source);
        TimeBasedDeduplicator dedup = new TimeBasedDeduplicator();
        String transformSpec = "{\n" +
                "    \"type\": \"js\",\n" +
//                "    \"script\": \"function main(obj) { var out = {}; var inp = JSON.parse(obj); out['id'] = inp['id']; return JSON.stringify(out); }\"\n" +
                "    \"script\": \"function main(obj) { var out = {}; var inp = JSON.parse(obj); out['id'] = inp['id']; out['trip_id'] = parseInt(inp['trip_id'], 10); out['observationDateTime'] = inp['observationDateTime']; out['route_id'] = inp['route_id']; out['trip_direction'] = inp['trip_direction']; out['actual_trip_start_time'] = inp['actual_trip_start_time']; out['last_stop_arrival_time'] = inp['last_stop_arrival_time'] ? new Date(new Date(inp['actual_trip_start_time']).toDateString() + ' ' + inp['last_stop_arrival_time'] + ' UTC').toISOString().replace('.000Z', '+05:30'): null; out['vehicle_label'] = inp['vehicle_label']; out['license_plate'] = inp['license_plate']; out['last_stop_id'] = parseInt(inp['last_stop_id'], 10); out['location.type'] = inp['location']['type']; out['location.coordinates'] = inp['location']['coordinates']; out['speed'] = parseFloat(inp['speed'], 10); out['trip_delay'] = parseFloat(inp['trip_delay'], 10); return JSON.stringify(out); }\"\n" +
                "  }";

        SingleOutputStreamOperator<Message> ds = so.keyBy((Message msg) -> msg.key)
                .process(new GenericProcessFunction(dedup))
                .flatMap(new JSProcessFunction(transformSpec));

        ds.addSink(new DumbSink());

        CompletableFuture<Void> handle = CompletableFuture.runAsync(() -> {
            try {
                env.execute("JS Test Job");
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
