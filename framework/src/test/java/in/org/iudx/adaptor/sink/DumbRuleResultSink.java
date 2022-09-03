package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.RuleResult;
import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

public class DumbRuleResultSink implements SinkFunction<RuleResult> {


  static CustomLogger logger;


  public DumbRuleResultSink() {
    logger = new CustomLogger(DumbSink.class, "unit_test");
  }

  /**
   * Statefulness for dumb things
   */
  public void open(Configuration config) throws Exception {
  }


  /**
   * Called for every message the incoming data
   */
  @Override
  public void invoke(RuleResult ruleResult, Context ctx) {
    logger.info(ruleResult.toString());
    logger.info((SerializationSchema<RuleResult>) result -> result.toString().getBytes());
  }
}

