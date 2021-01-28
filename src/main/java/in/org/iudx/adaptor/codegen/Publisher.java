package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.streaming.connectors.rabbitmq.RMQSinkPublishOptions;

/**
 * {@link Publisher} - Publisher interface
 *
 * TODO: 
 *  - Extend as needed
 *
 */
public interface Publisher extends Serializable {
  /* Exchange name for RMQ */
  String computeSinkName(Message message);
  /* Routing key for RMQ */
  String computeMessageTag(Message message);
}
