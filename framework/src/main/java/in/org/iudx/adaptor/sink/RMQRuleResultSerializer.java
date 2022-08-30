package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.datatypes.RuleResult;
import org.apache.flink.api.common.serialization.SerializationSchema;

public class RMQRuleResultSerializer implements SerializationSchema<RuleResult> {
  @Override
  public byte[] serialize(RuleResult ruleResult) {
    return ruleResult.getResult().getBytes();
  }
}

