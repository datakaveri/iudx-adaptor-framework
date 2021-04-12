package in.org.iudx.adaptor.codegen;

import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.api.common.serialization.SerializationSchema;

/* PO - Parser output. Thought to handle Message vs Array<Message> */

public interface PublishSerializer extends SerializationSchema<Message> {
  /* TODO: Generalize this too? */
  public byte[] serialize(Message inObj);
}
