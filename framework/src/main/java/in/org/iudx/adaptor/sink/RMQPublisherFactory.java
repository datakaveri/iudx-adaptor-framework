package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.codegen.RMQConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.datatypes.RuleResult;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;

public class RMQPublisherFactory<T> {
  public RMQSinkPublishOptions getPublisher(TypeInformation typeInformation,
                                            RMQConfig rmqConfig) {
    if (typeInformation.getTypeClass().equals(Message.class)) {
      return new RMQMessagePublisher(rmqConfig.getExchange(), rmqConfig.getRoutingKey());
    }
    if (typeInformation.getTypeClass().equals(RuleResult.class)) {
      return new RMQRuleResultPublisher();
    }
    return null;
  }
}
