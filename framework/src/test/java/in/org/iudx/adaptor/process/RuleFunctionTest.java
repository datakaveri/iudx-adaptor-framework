package in.org.iudx.adaptor.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.runtime.checkpoint.OperatorSubtaskState;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.streaming.api.operators.co.CoBroadcastWithKeyedOperator;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.util.KeyedTwoInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.TwoInputStreamOperatorTestHarness;
import org.apache.flink.util.Preconditions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class RuleFunctionTest {

  private static TwoInputStreamOperatorTestHarness<Message, Rule, RuleResult> testHarness;

  @BeforeAll
  public static void initialize() throws Exception {
    testHarness = getInitializedTestHarness(TypeInformation.of(String.class),
            (KeySelector<Message, String>) new IdentityKeySelector(),
            (KeyedBroadcastProcessFunction<String, Message, Rule, RuleResult>) new RuleFunction()
            , 1, 1, 0, null);
  }

  private static <KEY, IN1, IN2, OUT> TwoInputStreamOperatorTestHarness<IN1, IN2, OUT> getInitializedTestHarness(final TypeInformation<KEY> keyTypeInfo, final KeySelector<IN1, KEY> keyKeySelector, final KeyedBroadcastProcessFunction<KEY, IN1, IN2, OUT> function, final int maxParallelism, final int numTasks, final int taskIdx, final OperatorSubtaskState initState) throws Exception {
    final TwoInputStreamOperatorTestHarness<IN1, IN2, OUT> testHarness =
            new KeyedTwoInputStreamOperatorTestHarness<>(new CoBroadcastWithKeyedOperator<>(Preconditions.checkNotNull(function), Collections.singletonList(RuleStateDescriptor.ruleMapStateDescriptor)), keyKeySelector, null, keyTypeInfo, maxParallelism, numTasks, taskIdx);

    testHarness.setup();
    testHarness.open();

    return testHarness;
  }

  @Test
  void prElem1() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Rule r1 = mapper.readValue("{\"ruleId\":1,\"sqlQuery\":\"select * from TABLE where " +
            "`deviceId`='abc-456'\",\"type\":\"RULE\",\"windowMinutes\": 1000," +
            "\"sinkExchangeKey\": \"test\",\"sinkRoutingKey\": \"test\",\"resultColumnName\": " + "\"k1\"}", Rule.class);
//    Rule r1 = new Rule("select * from TABLE where `a`='c'", "https://out");
    r1.ruleId = 1;
    testHarness.processWatermark2(new Watermark(5L));
    testHarness.processElement2(r1, 10L);

    Message m1 = new Message().setKey("abc-456").setResponseBody("{\"deviceId\":\"abc-456\"," +
            "\"k1\":606,\"observationDateTime\":\"2022-08-25T15:09:35.961Z\"}");
    Message m2 = new Message().setKey("abc-123").setResponseBody("{\"deviceId\":\"abc-123\"," +
            "\"k1\":128,\"observationDateTime\":\"2022-08-25T15:09:41.285Z\"}");
    testHarness.processWatermark1(new Watermark(20L));
    testHarness.processElement1(m1, 30L);
    testHarness.processWatermark1(new Watermark(40L));
    testHarness.processElement1(m2, 50L);

    System.out.println(testHarness.getRecordOutput());
    System.out.println(testHarness.extractOutputStreamRecords());
    System.out.println(testHarness.extractOutputValues());
    System.out.println((testHarness.getOutput()));

//    Queue<Object> output = testHarness.getOutput();
//    for (Object out : output) {
//      System.out.println(out);
//    }

    testHarness.close();
  }

  private static class IdentityKeySelector implements KeySelector<Message, String> {
    private static final long serialVersionUID = 1L;

    @Override
    public String getKey(Message value) throws Exception {
      return value.key;
    }
  }
}
