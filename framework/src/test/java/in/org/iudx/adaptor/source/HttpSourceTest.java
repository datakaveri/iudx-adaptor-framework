package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.DataStreamSource;

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
import in.org.iudx.adaptor.codegen.SimpleBTestParser;
import in.org.iudx.adaptor.codegen.SimpleADeduplicator;

import in.org.iudx.adaptor.sink.AMQPSink;
import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.sink.StaticStringPublisher;


import in.org.iudx.adaptor.process.JoltTransformer;
import in.org.iudx.adaptor.process.JSProcessFunction;
import in.org.iudx.adaptor.process.JSPathProcessFunction;
import in.org.iudx.adaptor.process.TimeBasedDeduplicator;


public class HttpSourceTest {

  public static MiniClusterWithClientResource flinkCluster;


  private static StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

  private static final Logger LOGGER = LogManager.getLogger(HttpSourceTest.class);

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
  void simpleA() throws InterruptedException {


    SimpleATestTransformer trans = new SimpleATestTransformer();
    SimpleATestParser parser = new SimpleATestParser();
    SimpleADeduplicator dedup = new SimpleADeduplicator();


    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8888/auth/simpleA")
                                          .setRequestType("GET")
                                          .setHeader("Authorization",
                                                    "Basic YWRtaW46YWRtaW4=")
                                          .setPollingInterval(1000L);


    DataStreamSource<Message> so = env.addSource(new HttpSource<Message>(apiConfig, parser));
    /* Include process */
    so
        .keyBy((Message msg) -> msg.key)
        .process(new GenericProcessFunction(trans,dedup))
        .addSink(new DumbSink());


    try {
      env.execute("Simple Get");
    } catch (Exception e) {
      System.out.println(e);
    }
  }


  @Test
  void simpleB() throws InterruptedException {


    SimpleBTestParser parser = new SimpleBTestParser();
    SimpleATestTransformer trans = new SimpleATestTransformer();
    SimpleADeduplicator dedup = new SimpleADeduplicator();


    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8888/simpleB")
                                          .setRequestType("GET")
                                          .setPollingInterval(1000L);

    DataStreamSource<Message> so = env.addSource(new HttpSource<List<Message>>(apiConfig, parser));
    /* Include process */
    so
        .keyBy((Message msg) -> msg.key)
        .process(new GenericProcessFunction(trans,dedup))
        .addSink(new DumbSink());

    /* Passthrough
     *
    env.addSource(new HttpSource(apiConfig))
        .addSink(new DumbStringSink());
     **/

    try {
      env.execute("Simple Get");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  @Test
  void configBased() throws InterruptedException {


    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8888/simpleB")
                                          .setRequestType("GET")
                                          .setPollingInterval(1000L);

    String parseSpec = new JSONObject()
                                .put("containerPath", "$.data")
                                .put("keyPath", "$.deviceId")
                                .put("timestampPath", "$.time")
                                .toString();

    String dedupSpec = new JSONObject()
                              .put("deduplicationType", "timeBased")
                              .toString();


    String joltSpec = "[{ \"operation\": \"shift\", \"spec\": { \"time\": \"observationDateTime\", \"deviceId\": \"id\", \"k1\": \"k1\" } }, { \"operation\": \"modify-overwrite-beta\", \"spec\": { \"id\": \"=concat('datakaveri.org/123/', id)\" } }]";


    String transformSpec = new JSONObject().put("transformType", "jolt")
                                                .put("joltSpec", new JSONArray(joltSpec))
                                                .toString();

                                  
    JsonPathParser<Message[]> parser = new JsonPathParser<Message[]>(parseSpec);
    JoltTransformer trans = new JoltTransformer(transformSpec);
    TimeBasedDeduplicator dedup = new TimeBasedDeduplicator();

    /* Include process */
    env.addSource(new HttpSource<Message[]>(apiConfig, parser))
        .keyBy((Message msg) -> msg.key)
        .process(new GenericProcessFunction(trans, dedup))
        .addSink(new DumbSink());

    /* Passthrough
     *
    env.addSource(new HttpSource(apiConfig))
        .addSink(new DumbStringSink());
     **/

    try {
      env.execute("Simple Get");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

@Test
void dynUrl() throws InterruptedException {

    SimpleATestTransformer trans = new SimpleATestTransformer();
    SimpleATestParser parser = new SimpleATestParser();
    SimpleADeduplicator dedup = new SimpleADeduplicator();


    String script = "val = 'simpleA'";

    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8888/auth/path")
                                          .setRequestType("GET")
                                          .setHeader("Authorization",
                                                    "Basic YWRtaW46YWRtaW4=")
                                          .setPollingInterval(1000L)
                                          .setParamGenScript("url", "path", script);


    DataStreamSource<Message> so = env.addSource(new HttpSource<Message>(apiConfig, parser));
    /* Include process */
    so
        .keyBy((Message msg) -> msg.key)
        .process(new GenericProcessFunction(trans,dedup))
        .addSink(new DumbSink());


    try {
      env.execute("Simple Get");
    } catch (Exception e) {
      System.out.println(e);
    }
}


}

