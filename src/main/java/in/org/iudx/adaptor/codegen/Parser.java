package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.api.common.serialization.SerializationSchema;

public interface Parser extends SerializationSchema<Message> {
  public String getKey();
  public Instant getTimeIndex();
  public Message parse(String data);
  public byte[] serialize(Message inObj);
}
