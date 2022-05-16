package in.org.iudx.adaptor.process;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.process.GenericProcessFunction;
import in.org.iudx.adaptor.codegen.SimpleATestParser;
import in.org.iudx.adaptor.codegen.SimpleADeduplicator;
import in.org.iudx.adaptor.codegen.SimpleATestTransformer;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.source.HttpSource;


public class JSProcessTest {
  
  public static MiniClusterWithClientResource flinkCluster;


  private static StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();

  private static final Logger LOGGER = LogManager.getLogger(JSProcessTest.class);


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
  void processSimple() throws InterruptedException {

    SimpleATestParser parser = new SimpleATestParser();
    SimpleATestTransformer trans = new SimpleATestTransformer();
    SimpleADeduplicator dedup = new SimpleADeduplicator();

    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA")
                                          .setRequestType("GET")
                                          .setPollingInterval(1000L);

    String  script = "function tx(obj) { var out = {}; var inp = JSON.parse(obj); out[\"id\"] = \"datakaveri.org/a/b\" + inp[\"id\"]; out[\"k1\"] = inp[\"k1\"]; out[\"observationDateTime\"] = inp[\"time\"]; return JSON.stringify(out); }";
    String transformSpec = new JSONObject().put("script", script).toString();
    
    env.addSource(new HttpSource<Message>(apiConfig, parser))
        .keyBy((Message msg) -> msg.key)
        .process(new GenericProcessFunction(dedup, "test"))
        .flatMap(new JSProcessFunction(transformSpec))
        .addSink(new DumbSink());

    try {
      env.execute("test");
    } catch (Exception e) {
      System.out.println(e);
    }

  }

}

