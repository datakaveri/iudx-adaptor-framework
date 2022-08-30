package in.org.iudx.adaptor.sink;

import com.rabbitmq.client.AMQP;
import in.org.iudx.adaptor.datatypes.RuleResult;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;

public class RMQRuleResultPublisher implements RMQSinkPublishOptions<RuleResult> {

  @Override
  public String computeRoutingKey(RuleResult result) {
    return result.getSinkRoutingKey();
  }

  @Override
  public AMQP.BasicProperties computeProperties(RuleResult result) {
    // TODO check if headers is required
    return new AMQP.BasicProperties.Builder().expiration("10000").build();
  }

  @Override
  public String computeExchange(RuleResult result) {
    return result.getSinkExchangeKey();
  }
}