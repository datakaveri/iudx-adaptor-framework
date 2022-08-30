package in.org.iudx.adaptor.sink;

import com.rabbitmq.client.AMQP;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;

public class RMQMessagePublisher implements RMQSinkPublishOptions<Message> {

  private String exchange;
  private String routingKey;

  public RMQMessagePublisher(String exchange, String routingKey) {
    this.exchange = exchange;
    this.routingKey = routingKey;
  }

  @Override
  public String computeRoutingKey(Message message) {
    return this.routingKey;
  }

  @Override
  public AMQP.BasicProperties computeProperties(Message message) {
    // TODO check if headers is required
    return new AMQP.BasicProperties.Builder().expiration("10000").build();
  }

  @Override
  public String computeExchange(Message message) {
    return this.exchange;
  }
}
