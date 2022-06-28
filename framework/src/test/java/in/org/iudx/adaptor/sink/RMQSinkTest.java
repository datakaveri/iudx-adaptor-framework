package in.org.iudx.adaptor.sink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;



import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;


import java.util.Map;
import java.util.HashMap;

import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.sink.DumbStringSink;
import in.org.iudx.adaptor.process.GenericProcessFunction;
import in.org.iudx.adaptor.process.DumbProcess;


import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.codegen.SimpleATestTransformer;
import in.org.iudx.adaptor.codegen.SimpleATestParser;
import in.org.iudx.adaptor.codegen.SimpleADeduplicator;
import in.org.iudx.adaptor.source.HttpSource;
import in.org.iudx.adaptor.sink.AMQPSink;
import in.org.iudx.adaptor.sink.StaticStringPublisher;
import in.org.iudx.adaptor.codegen.RMQConfig;


import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.Collections;



import org.apache.flink.api.common.serialization.SimpleStringSchema;

public class RMQSinkTest {

  private AMQPSink amqpSink;
  public static MiniClusterWithClientResource flinkCluster;

  private static AMQP.BasicProperties props =
    new AMQP.BasicProperties.Builder()
    .headers(Collections.singletonMap("Test", "My Value"))
    .expiration("10000")
    .build();




  @BeforeAll
  public static void initialize() {
    flinkCluster =
      new MiniClusterWithClientResource(
          new MiniClusterResourceConfiguration.Builder()
          .setNumberSlotsPerTaskManager(2)
          .setNumberTaskManagers(1)
          .build());



  }

  @Test
  void simpleSink() throws InterruptedException {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    env.enableCheckpointing(10000L);
    CheckpointConfig config = env.getCheckpointConfig();
    config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

    SimpleATestTransformer trans = new SimpleATestTransformer();
    SimpleATestParser parser = new SimpleATestParser();
    SimpleADeduplicator dedup = new SimpleADeduplicator();


    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA")
                                          .setRequestType("GET")
                                          .setPollingInterval(1000L);


   RMQConfig amqconfig = new RMQConfig(); 
   amqconfig.setPublisher(new StaticStringPublisher("adaptor-test", "test"));
   amqconfig.builder.setUri("amqp://guest:guest@localhost:5672/");
   amqconfig.getConfig();
                      


    env.addSource(new HttpSource<Message>(apiConfig, parser))
        .keyBy((Message msg) -> msg.key)
        .process(new GenericProcessFunction(trans, dedup))
        //.process(new DumbProcess())
        //.addSink(new DumbStringSink());
        //.addSink(new RMQSink<String>(rmqConfig, new SimpleStringSchema(), publishOptions));
        .addSink(new AMQPSink(amqconfig));

    try {
      env.execute("Simple Get");
    } catch (Exception e) {
      System.out.println(e);
    }

  }

}
