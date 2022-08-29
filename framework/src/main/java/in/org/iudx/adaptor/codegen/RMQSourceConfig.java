package in.org.iudx.adaptor.codegen;

import java.io.Serializable;

import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;


public class RMQSourceConfig extends RMQConnectionConfig.Builder implements Serializable {

  private String queueName;

  public RMQSourceConfig() {
    super();
  }

  public RMQSourceConfig setQueueName(String queueName) {
    this.queueName = queueName;
    return this;
  }

  public String getQueueName() {
    return queueName;
  }
  
}
