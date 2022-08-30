package in.org.iudx.adaptor.sink;

import com.rabbitmq.client.AMQP;
import in.org.iudx.adaptor.codegen.*;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.process.GenericProcessFunction;
import in.org.iudx.adaptor.source.HttpSource;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class RMQSinkTest {

  public static MiniClusterWithClientResource flinkCluster;
  private static AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().headers(
          Collections.singletonMap("Test", "My Value")).expiration("10000").build();

  @BeforeAll
  public static void initialize() {
    flinkCluster = new MiniClusterWithClientResource(
            new MiniClusterResourceConfiguration.Builder().setNumberSlotsPerTaskManager(2)
                    .setNumberTaskManagers(1).build());


  }

  @Test
  void simpleSink() throws InterruptedException {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    env.enableCheckpointing(10000L);
    CheckpointConfig config = env.getCheckpointConfig();
    config.enableExternalizedCheckpoints(
            CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

    SimpleATestTransformer trans = new SimpleATestTransformer();
    SimpleATestParser parser = new SimpleATestParser();
    SimpleADeduplicator dedup = new SimpleADeduplicator();


    ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA")
            .setRequestType("GET").setPollingInterval(1000L);


    RMQConfig amqconfig = new RMQConfig();
    amqconfig.setUri("amqp://guest:guest@localhost:5672");
    amqconfig.setExchange("adaptor-test");
    amqconfig.setRoutingKey("test");
//   amqconfig.setPublisher(new StaticStringPublisher("adaptor-test", "test"));
//   amqconfig.builder.setUri("amqp://guest:guest@localhost:5672/");
//   amqconfig.getConfig();


    env.addSource(new HttpSource<Message>(apiConfig, parser)).keyBy((Message msg) -> msg.key)
            .process(new GenericProcessFunction(trans, dedup))
            //.process(new DumbProcess())
            //.addSink(new DumbStringSink());
            //.addSink(new RMQSink<String>(rmqConfig, new SimpleStringSchema(), publishOptions));
            //.addSink(new AMQPSink(amqconfig));
            .addSink(new RMQGenericSink<>(amqconfig, TypeInformation.of(Message.class)));
    try {
      env.execute("Simple Get");
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
