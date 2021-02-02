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


import java.util.Map;
import java.util.HashMap;

import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.process.GenericProcessFunction;


import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.codegen.SimpleTestTransformer;
import in.org.iudx.adaptor.codegen.SimpleTestParser;
import in.org.iudx.adaptor.codegen.SimpleDeduplicator;


public class HttpSourceTest {

  public static MiniClusterWithClientResource flinkCluster;
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
  }

  @Test
  void simpleGet() throws InterruptedException {

    StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    env.enableCheckpointing(10000L);
    CheckpointConfig config = env.getCheckpointConfig();
    config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);


    SimpleTestTransformer trans = new SimpleTestTransformer();
    SimpleTestParser parser = new SimpleTestParser();
    SimpleDeduplicator dedup = new SimpleDeduplicator();


    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8080/simpleA")
                                          .setRequestType("GET")
                                          .setKeyingProperty("deviceId")
                                          .setTimeIndexingProperty("time")
                                          .setPollingInterval(1000L)
                                          .setParser(parser)
                                          .setDeduplicator(dedup)
                                          .setTransformer(trans);


    /* Include process */
    env.addSource(new HttpSource(apiConfig))
        .keyBy((Message msg) -> msg.key)
        .process(new GenericProcessFunction(apiConfig))
        .addSink(new DumbSink(apiConfig.parser));

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
}

