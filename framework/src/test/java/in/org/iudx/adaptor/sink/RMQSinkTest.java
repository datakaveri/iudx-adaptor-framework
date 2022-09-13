package in.org.iudx.adaptor.sink;

import com.rabbitmq.client.AMQP;
import in.org.iudx.adaptor.codegen.*;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;
import in.org.iudx.adaptor.process.GenericProcessFunction;
import in.org.iudx.adaptor.process.RuleFunction;
import in.org.iudx.adaptor.source.HttpSource;
import in.org.iudx.adaptor.source.MessageWatermarkStrategy;
import in.org.iudx.adaptor.source.RMQGenericSource;
import in.org.iudx.adaptor.source.RMQPublisher;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class RMQSinkTest {
  private static StreamExecutionEnvironment env =
          StreamExecutionEnvironment.createLocalEnvironment();
  private static RMQPublisher pub;

  @BeforeAll
  public static void initialize() {
    StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    env.enableCheckpointing(10000L);

    pub = new RMQPublisher();
  }

  @Test
  void simpleSink() throws InterruptedException {
    SimpleATestTransformer trans = new SimpleATestTransformer();
    SimpleATestParser parser = new SimpleATestParser();
    SimpleADeduplicator dedup = new SimpleADeduplicator();


    ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA")
            .setRequestType("GET").setPollingInterval(1000L);


    RMQConfig amqconfig = new RMQConfig();
    amqconfig.setUri("amqp://guest:guest@localhost:5672");
    amqconfig.setExchange("adaptor-test");
    amqconfig.setRoutingKey("test");

    env.addSource(new HttpSource<Message>(apiConfig, parser)).keyBy((Message msg) -> msg.key)
            .process(new GenericProcessFunction(trans, dedup))
            .addSink(new RMQGenericSink<>(amqconfig, TypeInformation.of(Message.class)));
    try {
      env.execute("Simple Get");
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  @Test
  void ruleResuletTest() throws Exception {
    pub.initialize();
    pub.sendRuleMessage();

    int numMsgs = 5;
    for (int i = 0; i < numMsgs; i++) {
      pub.sendMessage(i);
    }

    String parseSpecObj = new JSONObject().put("timestampPath", "$.observationDateTime")
            .put("staticKey", "ruleTest")
            .put("expiry", 200)
            .put("inputTimeFormat", "yyyy-MM-dd HH:mm:ss")
            .put("outputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX").toString();

    RMQConfig config = new RMQConfig();
    config.setUri("amqp://guest:guest@localhost:5672");
    config.setQueueName("adaptor-test");
    RMQGenericSource source = new RMQGenericSource<Message>(config,
            TypeInformation.of(Message.class), "test", parseSpecObj);

    RMQConfig ruleConfig = new RMQConfig();
    ruleConfig.setUri("amqp://guest:guest@localhost:5672");
    ruleConfig.setQueueName("rules-test");
    RMQGenericSource ruleSource = new RMQGenericSource<Rule>(ruleConfig, TypeInformation.of(Rule.class),
            "test", null);

    DataStreamSource<Message> so = env.addSource(source);
    DataStreamSource<Rule> rules = env.addSource(ruleSource);

    BroadcastStream<Rule> ruleBroadcastStream = rules.broadcast(
            RuleStateDescriptor.ruleMapStateDescriptor);
    SingleOutputStreamOperator<RuleResult> ds = so.assignTimestampsAndWatermarks(
                    new MessageWatermarkStrategy()).keyBy((Message msg) -> msg.key)
            .connect(ruleBroadcastStream).process(new RuleFunction()).setParallelism(1);

    RMQConfig rmqConfig = new RMQConfig();
    rmqConfig.setUri("amqp://guest:guest@localhost:5672");
    ds.addSink(new RMQGenericSink<>(rmqConfig, TypeInformation.of(RuleResult.class)));

    CompletableFuture<Void> handle = CompletableFuture.runAsync(() -> {
      try {
        env.execute("Rule Result Test");
      } catch (Exception e) {
        System.out.println(e);
      }
    });
    try {
      handle.get(20, TimeUnit.SECONDS);
    } catch (TimeoutException | ExecutionException e) {
      handle.cancel(true); // this will interrupt the job execution thread, cancel and close the job
    }
  }
}
