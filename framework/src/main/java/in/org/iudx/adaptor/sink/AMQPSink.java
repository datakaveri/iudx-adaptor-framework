package in.org.iudx.adaptor.sink;

import org.apache.flink.streaming.connectors.rabbitmq.RMQSink;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.RMQConfig;
import org.apache.flink.api.common.serialization.SerializationSchema;

import in.org.iudx.adaptor.sink.StaticStringPublisher;


/**
 * {@link AMQPSink} - Extends Flink native RMQSink {@link RMQSink}
 * This is a wrapper to extend functionality to RMQSink for posterity.
 */
public class AMQPSink extends RMQSink<Message> {

  private RMQConfig rmqConfig;

  public AMQPSink(RMQConfig rmqConfig) {
    super(rmqConfig.connectionConfig, rmqConfig.publisher, rmqConfig);
    this.rmqConfig = rmqConfig;
  }
}
