package in.org.iudx.adaptor.source;

import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.sink.DumbSink;
import in.org.iudx.adaptor.sink.DumbWatermarkSink;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.MiniClusterWithClientResource;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RMQITTest {

  public static MiniClusterWithClientResource flinkCluster;
  private static StreamExecutionEnvironment env =
          StreamExecutionEnvironment.createLocalEnvironment();
  private static RMQPublisher pub;


  @BeforeAll
  public static void initialize() {

    StreamExecutionEnvironment.createLocalEnvironment();
    ExecutionConfig executionConfig = env.getConfig();
    env.setParallelism(1);
    executionConfig.setAutoWatermarkInterval(1);


   env.enableCheckpointing(10000L);
   CheckpointConfig config = env.getCheckpointConfig();
   config.enableExternalizedCheckpoints(
           CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);


    pub = new RMQPublisher();
  }


  @Test
  void testA() throws Exception {

//    int numMsgs = 2;
//    pub.initialize();
//    for (int i = 0; i < numMsgs; i++) {
//      pub.sendMessage(i);
//    }

    String parseSpecObj = new JSONObject()
            .put("timestampPath", "$.observationDateTime")
            .put("containerPath", "$")
            .put("messageContainer", "array")
            .put("keyPath", "$.id")
            .put("inputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX")
            .put("outputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX").toString();

    RMQConfig config = new RMQConfig();
    config.setUri("amqp://guest:guest@localhost:5672");
    config.setQueueName("adaptor-test");

    JsonPathParser<Message> parser = new JsonPathParser<>(parseSpecObj);

    RMQGenericSource source = new RMQGenericSource<Message>(config,
            TypeInformation.of(Message.class), "test", parser);

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

  @Test
  void testWatermark() throws Exception {

//    int numMsgs = 5;
//    pub.initialize();
//    for (int i = 0; i < numMsgs; i++) {
//      pub.sendMessage(i);
//    }

    String parseSpecObj = new JSONObject()
            .put("timestampPath", "$.time")
            .put("keyPath", "$.id")
            .put("inputTimeFormat", "yyyy-MM-dd HH:mm:ss")
            .put("outputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX")
            .toString();

    RMQConfig config = new RMQConfig();
    config.setUri("amqp://guest:guest@localhost:5672");
    config.setQueueName("adaptor-test");

    JsonPathParser<List<Message>> parser = new JsonPathParser<List<Message>>(parseSpecObj);

    RMQGenericSource source = new RMQGenericSource<List<Message>>(config,
            TypeInformation.of(Message.class), "test", parser);

    DataStreamSource<Message> so = env.addSource(source);
//    so.assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());

    so.assignTimestampsAndWatermarks(new MessageWatermarkStrategy())
            .addSink(new DumbWatermarkSink());

    CompletableFuture<Void> handle = CompletableFuture.runAsync(() -> {
      try {
        env.execute("Simple Get");
      } catch (Exception e) {
        System.out.println(e);
      }
    });
    try {
      handle.get(40, TimeUnit.SECONDS);
    } catch (TimeoutException | ExecutionException e) {
      handle.cancel(true); // this will interrupt the job execution thread, cancel and close the job
    }


  }
}
