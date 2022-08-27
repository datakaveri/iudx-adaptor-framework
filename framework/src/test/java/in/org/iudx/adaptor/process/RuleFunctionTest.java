package in.org.iudx.adaptor.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sisyphsu.dateparser.DateParserUtils;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;
import org.apache.commons.collections.IteratorUtils;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class RuleFunctionTest {

  private static TwoInputStreamOperatorTestHarness<Message, Rule, RuleResult> testHarness;
  private static RuleFunction ruleFunction = new RuleFunction();

  @BeforeAll
  public static void initialize() throws Exception {
    testHarness = getInitializedTestHarness(TypeInformation.of(String.class),
            (KeySelector<Message, String>) new IdentityKeySelector(),
            (KeyedBroadcastProcessFunction<String, Message, Rule, RuleResult>) ruleFunction, 1, 1,
            0, null);
  }

  private static <KEY, IN1, IN2, OUT> TwoInputStreamOperatorTestHarness<IN1, IN2, OUT> getInitializedTestHarness(
          final TypeInformation<KEY> keyTypeInfo, final KeySelector<IN1, KEY> keyKeySelector,
          final KeyedBroadcastProcessFunction<KEY, IN1, IN2, OUT> function,
          final int maxParallelism, final int numTasks, final int taskIdx,
          final OperatorSubtaskState initState) throws Exception {
    final TwoInputStreamOperatorTestHarness<IN1, IN2, OUT> testHarness =
            new KeyedTwoInputStreamOperatorTestHarness<>(
            new CoBroadcastWithKeyedOperator<>(Preconditions.checkNotNull(function),
                    Collections.singletonList(RuleStateDescriptor.ruleMapStateDescriptor)),
            keyKeySelector, null, keyTypeInfo, maxParallelism, numTasks, taskIdx);

    testHarness.setup();
    testHarness.open();

    return testHarness;
  }

  @Test
  void prElem1() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    Rule r1 = mapper.readValue(
            "{\"ruleId\":1,\"sqlQuery\":\"select * from TABLE where " + "`deviceId`='abc-456'\"," + "\"type\":\"RULE\",\"windowMinutes\": 1000," + "\"sinkExchangeKey\": " + "\"test\",\"sinkRoutingKey\": \"test\"}",
            Rule.class);
//    Rule r1 = new Rule("select * from TABLE where `a`='c'", "https://out");
//    r1.ruleId = 1;
    testHarness.processWatermark2(new Watermark(5L));
    testHarness.processElement2(r1, 10L);

    Message m1 = new Message().setKey("abc-456").setResponseBody(
            "{\"deviceId\":\"abc-456\"," + "\"k1\":606,\"observationDateTime\":\"2022-08-25T15:09"
                    + ":35.961Z\"}");
    Message m2 = new Message().setKey("abc-456").setResponseBody(
            "{\"deviceId\":\"abc-456\"," + "\"k1\":128,\"observationDateTime\":\"2022-08-25T15:09"
                    + ":41.285Z\"}");
    testHarness.processWatermark1(new Watermark(20L));
    testHarness.processElement1(m1, 30L);
    testHarness.processWatermark1(new Watermark(40L));
    testHarness.processElement1(m2, 50L);

//    System.out.println(testHarness.getRecordOutput());
//    System.out.println(testHarness.extractOutputStreamRecords());
//    System.out.println(testHarness.extractOutputValues());
    System.out.println((testHarness.getOutput()));

//    Queue<Object> output = testHarness.getOutput();
//    for (Object out : output) {
//      System.out.println(out);
//    }

    testHarness.close();
  }


  @Test
  void testTimer() throws Exception {
    ListStateDescriptor<LinkedHashMap<String, Object>> listStateDescriptor =
            new ListStateDescriptor<>(
            "listState", TypeInformation.of(new TypeHint<LinkedHashMap<String, Object>>() {
    }));
    ListState<LinkedHashMap<String, Object>> listState = ruleFunction.getRuntimeContext()
            .getListState(listStateDescriptor);
    ObjectMapper mapper = new ObjectMapper();
    Rule r1 = mapper.readValue(
            "{\"ruleId\":1,\"sqlQuery\":\"select * from TABLE where " + "`deviceId`='abc-456'\"," + "\"type\":\"RULE\",\"windowMinutes\": 1," + "\"sinkExchangeKey\": " + "\"test\",\"sinkRoutingKey\": \"test\"}",
            Rule.class);
//    Rule r1 = new Rule("select * from TABLE where `a`='c'", "https://out");
//    r1.ruleId = 1;
    testHarness.processWatermark2(new Watermark(5L));
    testHarness.processElement2(r1, 10L);

    Instant msg1Time = Instant.now();

    Message m1 = new Message().setKey("abc-456").setResponseBody(
            "{\"deviceId\":\"abc-456\"," + "\"k1\":606,\"observationDateTime\":\"" + msg1Time.toString() + "\"}");

    LocalDateTime localDateTime = DateParserUtils.parseDateTime(msg1Time.toString());
    long timestamp = Timestamp.valueOf(localDateTime).toInstant().toEpochMilli();

    testHarness.processElement1(m1, timestamp);

    System.out.println(testHarness.numEventTimeTimers());
    testHarness.processBothWatermarks(new Watermark(timestamp));

    Instant msg2Time = Instant.now();
    Message m2 = new Message().setKey("abc-456").setResponseBody(
            "{\"deviceId\":\"abc-456\"," + "\"k1\":128,\"observationDateTime\":\"" + msg2Time.toString() + "\"}");

    localDateTime = DateParserUtils.parseDateTime(msg2Time.toString());
    timestamp = Timestamp.valueOf(localDateTime).toInstant().toEpochMilli();

    testHarness.processElement1(m2, timestamp);

    System.out.println("List state before executing event timer");
    Iterator<LinkedHashMap<String, Object>> iterator = listState.get().iterator();
    List<LinkedHashMap<String, Object>> list = IteratorUtils.toList(iterator);
    System.out.println(list);

    testHarness.processBothWatermarks(new Watermark(timestamp));

    System.out.println("List state after executing event timer");
    iterator = listState.get().iterator();
    list = IteratorUtils.toList(iterator);
    System.out.println(list);



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
