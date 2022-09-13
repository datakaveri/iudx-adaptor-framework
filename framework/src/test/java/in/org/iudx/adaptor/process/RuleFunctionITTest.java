package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;
import in.org.iudx.adaptor.sink.DumbRuleResultSink;
import in.org.iudx.adaptor.source.MessageWatermarkStrategy;
import in.org.iudx.adaptor.source.RMQGenericSource;
import in.org.iudx.adaptor.source.RMQPublisher;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RuleFunctionITTest {
  private static final Logger LOGGER = LogManager.getLogger(RuleFunctionITTest.class);
  private static StreamExecutionEnvironment env =
          StreamExecutionEnvironment.createLocalEnvironment();

  private static RMQPublisher pub;

  @BeforeAll
  public static void initialize() {
    LOGGER.debug("Info: Testing");
    StreamExecutionEnvironment.createLocalEnvironment();
    env.setParallelism(1);

    env.enableCheckpointing(10000L);

    pub = new RMQPublisher();
  }


  @Test
  void testRule() throws Exception {
    pub.initialize();
    pub.sendRuleMessage();

    int numMsgs = 5;
    for (int i = 0; i < numMsgs; i++) {
      pub.sendMessage(i);
    }

    String parseSpecObj = new JSONObject().put("timestampPath", "$.observationDateTime")
//            .put("keyPath", "$.id")
            .put("staticKey", "ruleTest")
//            .put("expiry", 200)
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

    ds.addSink(new DumbRuleResultSink());
    CompletableFuture<Void> handle = CompletableFuture.runAsync(() -> {
      try {
        env.execute("Simple Get");
      } catch (Exception e) {
        System.out.println(e);
      }
    });
    try {
      // increased time to 4 minutes to test timer
      handle.get(4, TimeUnit.MINUTES);
    } catch (TimeoutException | ExecutionException e) {
      handle.cancel(true); // this will interrupt the job execution thread, cancel and close the job
    }
  }
}
