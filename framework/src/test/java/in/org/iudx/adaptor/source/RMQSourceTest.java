package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import java.lang.Thread;

import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.rabbitmq.RMQDeserializationSchema;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.flink.streaming.util.AbstractStreamOperatorTestHarness;
import org.apache.flink.streaming.api.operators.StreamSource;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.api.common.state.OperatorStateStore;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.mockito.Mockito;
import org.apache.flink.configuration.Configuration;


import in.org.iudx.adaptor.codegen.RMQSourceConfig;
import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.Message;

public class RMQSourceTest {

  private static RMQPublisher pub;
  private static RMQGenericSource source;
  private Configuration config = new Configuration();

  private Thread sourceThread;
  private volatile long messageId;
  private boolean generateCorrelationIds;
  private volatile Exception exception;


  @BeforeEach
  public void beforeTest() throws Exception {

    RMQSourceConfig config = new RMQSourceConfig();
    config.setUri("amqp://guest:guest@localhost:5672");
    config.setQueueName("adaptor-test");

    RMQDeserializationSchema deser 
      = RMQDeserializerFactory.getDeserializer(TypeInformation.of(Rule.class),
                                                "test", null);

    source = new RMQGenericSource<Rule>(config, deser);

    DummyRuleSourceContext.numElementsCollected = 0;
    sourceThread =
      new Thread(
          new Runnable() {
            @Override
            public void run() {
              try {
                source.run(new DummyRuleSourceContext());
              } catch (Exception e) {
                exception = e;
              }
            }
          });
  }

  @AfterEach
  public void afterTest() throws Exception {
    source.cancel();
    sourceThread.join();
  }



  @BeforeAll
  public static void initialize() {
    pub = new RMQPublisher();
  }

  @Test
  public void ruleSource() throws Exception {

    int numMsgs = 2;
    pub.initialize();
    for (int i=0;i< numMsgs;i++) {
      pub.sendRuleMessage();
      pub.sendRuleMessage();
    }

    AbstractStreamOperatorTestHarness<Rule> testHarness =
      new AbstractStreamOperatorTestHarness<Rule>(new StreamSource<>(source), 1, 1, 0);
    testHarness.setup();
    testHarness.open();

    sourceThread.start();
    Thread.sleep(2000);

    while (DummyRuleSourceContext.numElementsCollected < numMsgs) {
      Thread.sleep(5);
    }
    sourceThread.stop();
  }


  private static class DummyRuleSourceContext implements SourceFunction.SourceContext<Rule> {

    private static final Object lock = new Object();
    private static long numElementsCollected;

    public DummyRuleSourceContext() {
      numElementsCollected = 0;
    }

    @Override
    public void collect(Rule element) {
      numElementsCollected++;
    }

    @Override
    public void collectWithTimestamp(Rule element, long timestamp) {}

    @Override
    public void emitWatermark(Watermark mark) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void markAsTemporarilyIdle() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object getCheckpointLock() {
      return lock;
    }

    @Override
    public void close() {}
  }


}
