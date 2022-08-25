package in.org.iudx.adaptor.process;

import org.junit.jupiter.api.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.jupiter.api.BeforeAll;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import org.apache.flink.shaded.guava30.com.google.common.collect.ImmutableList;

import com.fasterxml.jackson.databind.ObjectMapper;

import in.org.iudx.adaptor.sql.JsonArrayListTable;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.utils.JsonFlatten;
import in.org.iudx.adaptor.descriptors.RuleStateDescriptor;

import org.apache.flink.runtime.state.PartitionableListState;
import org.apache.flink.streaming.util.KeyedTwoInputStreamOperatorTestHarness;
import org.apache.flink.streaming.util.TwoInputStreamOperatorTestHarness;
import org.apache.flink.streaming.api.operators.co.CoBroadcastWithKeyedOperator;
import org.apache.flink.util.Preconditions;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.operators.TwoInputStreamOperator;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.runtime.checkpoint.OperatorSubtaskState;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.util.Collector;
import org.apache.flink.streaming.util.AbstractStreamOperatorTestHarness;

public class RuleFunctionTest {


  private static TwoInputStreamOperatorTestHarness<Message,Rule,String> testHarness;


  private static class IdentityKeySelector implements KeySelector<Message, String> {
      private static final long serialVersionUID = 1L;

      @Override
      public String getKey(Message value) throws Exception {
          return value.key;
      }
  }


  @BeforeAll
  public static void initialize() throws Exception {

    testHarness = getInitializedTestHarness(TypeInformation.of(String.class),
                      (KeySelector<Message,String>) new IdentityKeySelector(),
                      (KeyedBroadcastProcessFunction) new RuleFunction(),
                       1, 1, 0, null);



  }

  @Test
  public void prElem1() throws Exception {

    Rule r1 = new Rule("select * from listState.state where `a`='c'", "https://out");
    r1.ruleId = 1;
    testHarness.processWatermark2(new Watermark(5L));
    testHarness.processElement2(r1, 10L);

     Message m1 = new Message().setKey("b").setResponseBody("{\"a\": \"b\"}");
     Message m2 = new Message().setKey("c").setResponseBody("{\"a\": \"c\"}");
     testHarness.processWatermark1(new Watermark(20L));
     testHarness.processElement1(m1, 30L);
     testHarness.processWatermark1(new Watermark(40L));
     testHarness.processElement1(m2, 50L);

     Queue<Object> output = testHarness.getOutput();
     for(Object out: output) {
       System.out.println(out);
     }


  }




  private static <KEY, IN1, IN2, OUT>
    TwoInputStreamOperatorTestHarness<IN1, IN2, OUT> getInitializedTestHarness(
        final TypeInformation<KEY> keyTypeInfo,
        final KeySelector<IN1, KEY> keyKeySelector,
        final KeyedBroadcastProcessFunction<KEY, IN1, IN2, OUT> function,
        final int maxParallelism,
        final int numTasks,
        final int taskIdx,
        final OperatorSubtaskState initState)
      throws Exception {

      final TwoInputStreamOperatorTestHarness<IN1, IN2, OUT> testHarness =
        new KeyedTwoInputStreamOperatorTestHarness<>(
            new CoBroadcastWithKeyedOperator<>(
              Preconditions.checkNotNull(function),
              Collections.singletonList(RuleStateDescriptor.ruleMapStateDescriptor)),
            keyKeySelector,
            null,
            keyTypeInfo,
            maxParallelism,
            numTasks,
            taskIdx);

      testHarness.setup();
      testHarness.open();

      return testHarness;
        }


}
