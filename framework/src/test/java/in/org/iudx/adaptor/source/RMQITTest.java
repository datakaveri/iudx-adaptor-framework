package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.apache.flink.test.util.MiniClusterResourceConfiguration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.JSONObject;
import org.json.JSONArray;

import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.codegen.RMQSourceConfig;
import in.org.iudx.adaptor.datatypes.Message;

public class RMQITTest {

  public static MiniClusterWithClientResource flinkCluster;
  private static StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironment();
  private static RMQPublisher pub;


  @BeforeAll
  public static void initialize() {
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


    pub = new RMQPublisher();
  }



  @Test
  void testA() throws Exception {

    int numMsgs = 2;
    pub.initialize();
    for (int i=0;i< numMsgs;i++) {
      pub.sendMessage();
      pub.sendMessage();
    }

    String parseSpecObj = new JSONObject()
      .put("timestampPath", "$.time")
      .put("keyPath", "$.id")
      .put("inputTimeFormat","yyyy-MM-dd HH:mm:ss")
      .put("outputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX")
      .toString();

    RMQSourceConfig config = new RMQSourceConfig();
    config.setUri("amqp://guest:guest@localhost:5672");
    config.setQueueName("adaptor-test");
    RMQDeserializationSchema deser = new RMQMessageDeserializer("test", parseSpecObj);
    RMQGenericSource source = new RMQGenericSource<Message>(config, deser);

    DataStreamSource<Message> so = env.addSource(source);
    // so.assignTimestampsAndWatermarks(new MessageWatermarkStrategy());
    so.addSink(new DumbSink());

    CompletableFuture<Void> handle = CompletableFuture.runAsync(() -> {
      try {
        env.execute("Simple Get");
      } catch (Exception e) {
        System.out.println(e);
      }
    });
    try {
      handle.get(10, TimeUnit.SECONDS);
    } catch (TimeoutException | ExecutionException e) {
      handle.cancel(true); // this will interrupt the job execution thread, cancel and close the job
    }


  }



}
