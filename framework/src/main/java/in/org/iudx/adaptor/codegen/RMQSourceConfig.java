package in.org.iudx.adaptor.codegen;

import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.io.Serializable;


public class RMQSourceConfig extends RMQConnectionConfig.Builder implements Serializable {

  private String queueName;

  private String exchange;

  private String routingKey;

  public RMQSourceConfig() {
    super();
  }

  public String getQueueName() {
    return queueName;
  }

  public RMQSourceConfig setQueueName(String queueName) {
    this.queueName = queueName;
    return this;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  public String getRoutingKey() {
    return routingKey;
  }

  public void setRoutingKey(String routingKey) {
    this.routingKey = routingKey;
  }

}
