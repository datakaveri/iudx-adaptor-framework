package in.org.iudx.adaptor.codegen;

import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig.Builder;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;

import in.org.iudx.adaptor.datatypes.Message;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.util.Collections;

/**
 * {@link RMQConfig} - Extends Flink native rmq config builder {@link RMQConnectionConfig}
 * This is a wrapper to extend more connection configurations in posterity.
 */
public class RMQConfig implements SinkConfig<RMQConnectionConfig>,RMQSinkPublishOptions<Message> {

  private static final long serialVersionUID = 13L;
  private boolean mandatory = false;
  private boolean immediate = false;

  /* User provided */
  public Publisher publisher;
  public transient Builder builder;
  public RMQConnectionConfig connectionConfig;


  public RMQConfig() {
    builder = new Builder();
  }

  public RMQConnectionConfig getConfig() {
    this.connectionConfig = builder.build();
    return this.connectionConfig;
  }

  /* TODO: Why? */
  private static AMQP.BasicProperties props =
    new AMQP.BasicProperties.Builder()
    .headers(Collections.singletonMap("iudx-adaptor", "iudx-adaptor"))
    .expiration("10000")
    .build();

  public RMQConfig setPublisher(Publisher publisher) {
    this.publisher = publisher;
    return this;
  }

  @Override
  public String computeRoutingKey(Message msg) {
    return publisher.computeMessageTag(msg);
  }

  @Override
  public BasicProperties computeProperties(Message msg) {
    return props;
  }

  @Override
  public String computeExchange(Message msg) {
    return publisher.computeSinkName(msg);
  }

  @Override
  public boolean computeMandatory(Message msg) {
    return mandatory;
  }

  @Override
  public boolean computeImmediate(Message msg) {
    return immediate;
  }
}

