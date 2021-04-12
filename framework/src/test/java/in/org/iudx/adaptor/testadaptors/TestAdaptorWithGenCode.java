package in.org.iudx.adaptor.testadaptors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;


import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.process.GenericProcessFunction;


import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.codegen.SimpleATestTransformer;
import in.org.iudx.adaptor.codegen.SimpleATestParser;
import in.org.iudx.adaptor.codegen.SimpleBTestParser;
import in.org.iudx.adaptor.codegen.SimpleADeduplicator;
import in.org.iudx.adaptor.source.HttpSource;
import in.org.iudx.adaptor.source.JsonPathParser;
import in.org.iudx.adaptor.process.JSProcessFunction;

import in.org.iudx.adaptor.sink.AMQPSink;
import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.sink.StaticStringPublisher;


import in.org.iudx.adaptor.process.JoltTransformer;
import in.org.iudx.adaptor.process.TimeBasedDeduplicator;


public class TestAdaptorWithGenCode {

  public static MiniClusterWithClientResource flinkCluster;


  private static StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

  private static final Logger LOGGER = LogManager.getLogger(TestAdaptorWithGenCode.class);

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

    env.enableCheckpointing(10000L);
    CheckpointConfig config = env.getCheckpointConfig();
    config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);



  }

  @Test
  void simpleB() throws InterruptedException {
    ApiConfig apiConfig = new ApiConfig().setUrl("http://localhost:8888/simpleB").setRequestType("GET").setPollingInterval(1000);
    String parseSpec = "{\"messageContainer\":\"array\",\"timestampPath\":\"$.time\",\"keyPath\":\"$.deviceId\",\"type\":\"json\",\"containerPath\":\"$.data\"}";
    JsonPathParser<Message[]> parser = new JsonPathParser<Message[]>(parseSpec);
    TimeBasedDeduplicator dedup = new TimeBasedDeduplicator();
    String transformSpec = "{\"type\":\"js\",\"script\":\"function tx(obj) { var out = {}; var inp = JSON.parse(obj); out[\\\"id\\\"] = \\\"datakaveri.org/a/b\\\" + inp[\\\"id\\\"]; out[\\\"k1\\\"] = inp[\\\"k1\\\"]; out[\\\"observationDateTime\\\"] = inp[\\\"time\\\"]; return JSON.stringify(out); }\"}";
    // RMQConfig rmqConfig = new RMQConfig();
    // rmqConfig.setPublisher(new StaticStringPublisher("adaptor-test", "test"));
    // rmqConfig.builder.setUri("amqp://mockrmq").setPort(5672).setUserName("guest").setPassword("guest");
    // rmqConfig.getConfig();
    SingleOutputStreamOperator<Message> ds = env.addSource(new HttpSource<Message[]>(apiConfig, parser)).keyBy((Message msg) -> msg.key).process(new GenericProcessFunction(dedup)).flatMap(new JSProcessFunction(transformSpec));
    ds.addSink(new DumbSink());
    try {
      env.execute("test");
    } catch (Exception e) {
    }

  }


}

