package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.api.common.serialization.SerializationSchema;

/* PO - Parser output. Thought to handle Message vs Array<Message> */

public interface Parser<PO> extends SerializationSchema<Message> {
  public PO parse(String data);
  /* TODO: Generalize this too? */
  public byte[] serialize(Message inObj);
}
