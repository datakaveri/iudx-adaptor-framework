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

import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.sink.DumbStringSink;
import in.org.iudx.adaptor.process.GenericProcessFunction;
import in.org.iudx.adaptor.process.DumbProcess;


import in.org.iudx.adaptor.source.HttpSource;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.process.TimeBasedDeduplicator;

import in.org.iudx.adaptor.testadaptors.SimpleATestTransformer;
import in.org.iudx.adaptor.testadaptors.SimpleBTestParser;
import in.org.iudx.adaptor.testadaptors.SimpleADeduplicator;


public class VaranasiAqm {

  public static MiniClusterWithClientResource flinkCluster;
  private static final Logger LOGGER = LogManager.getLogger(VaranasiAqm.class);
  private static StreamExecutionEnvironment env;

  @BeforeAll
  public static void initialize() {
    LOGGER.debug("Info: Testing");
    flinkCluster =
      new MiniClusterWithClientResource(
          new MiniClusterResourceConfiguration.Builder()
          .setNumberSlotsPerTaskManager(2)
          .setNumberTaskManagers(1)
          .build());


    env = StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    env.enableCheckpointing(10000L);
    CheckpointConfig config = env.getCheckpointConfig();
    config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);


  }



  @Test
  void simpleB() throws InterruptedException {


    SimpleBTestParser parser = new SimpleBTestParser();
    SimpleATestTransformer trans = new SimpleATestTransformer();

    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://220.227.12.146:8081/Equipment/GetEnvrDataStatus?EqpId=all")
                                          .setRequestType("GET")
                                          .setPollingInterval(10000L);


    /* Include process */
    env.addSource(new HttpSource<Message[]>(apiConfig, parser))
        .keyBy((Message msg) -> msg.key)
        .process(new GenericProcessFunction(trans, new TimeBasedDeduplicator()))
        .addSink(new DumbSink(parser));

    // env.addSource(new HttpSource<Message[]>(apiConfig, parser))
    //    .keyBy((Message msg) -> msg.key)
    //    .process(new DumbProcess())
    //    .addSink(new DumbStringSink());

    try {
      env.execute("Adaptor");
    } catch (Exception e) {
      System.out.println(e);
    }
  }


}

