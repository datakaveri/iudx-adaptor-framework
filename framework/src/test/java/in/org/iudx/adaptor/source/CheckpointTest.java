package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import in.org.iudx.adaptor.process.DumbProcess;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import java.util.Optional;
import org.apache.flink.util.SerializedThrowable;

import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.jobgraph.SavepointRestoreSettings;



import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;
import org.apache.flink.client.program.ClusterClient;

import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.state.api.BootstrapTransformation;
import org.apache.flink.state.api.OperatorTransformation;
import org.apache.flink.state.api.Savepoint;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.sink.DumbStringSink;
import in.org.iudx.adaptor.process.StatefulProcess;
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
import in.org.iudx.adaptor.process.TimeBasedDeduplicator;




public class CheckpointTest {

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
    env.setStateBackend(new EmbeddedRocksDBStateBackend());
    env.getCheckpointConfig().setCheckpointStorage("file:///tmp/checkpoints");
    config.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);


  }

  @Test
  void constantA() throws InterruptedException {


    SimpleATestTransformer trans = new SimpleATestTransformer();
    SimpleATestParser parser = new SimpleATestParser();
    SimpleADeduplicator dedup = new SimpleADeduplicator();


    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8888/constantA")
                                          .setRequestType("GET")
                                          .setPollingInterval(1000L);


    DataStreamSource<Message> so = env.addSource(new HttpSource<Message>(apiConfig, parser));
    /* Include process */
    so
        .keyBy((Message msg) -> msg.key)
        .process(new DumbProcess(trans,dedup))
        .uid("gpf")
        .addSink(new DumbSink());


    try {
      env.execute("Simple Get");
    } catch (Exception e) {
      System.out.println(e);
    }
  }



}


