package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.api.common.serialization.SerializationSchema;


public class RMQMessageSerializer implements SerializationSchema<Message> {

  @Override
  public byte[] serialize(Message message) {
    return message.toString().getBytes();
  }
}
