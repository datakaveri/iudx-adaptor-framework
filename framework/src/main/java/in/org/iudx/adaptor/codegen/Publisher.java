package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;
import org.apache.flink.api.common.serialization.SerializationSchema;

/**
 * {@link Publisher} - Publisher interface
 *
 * TODO: 
 *  - Extend as needed
 *
 */
public interface Publisher extends Serializable, SerializationSchema<Message> {
  /* Exchange name for RMQ */
  String computeSinkName(Message message);
  /* Routing key for RMQ */
  String computeMessageTag(Message message);

  byte[] serialize(Message inObj);
}
