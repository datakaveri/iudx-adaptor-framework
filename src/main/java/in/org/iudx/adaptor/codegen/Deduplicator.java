package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import in.org.iudx.adaptor.datatypes.Message;

public interface Deduplicator extends Serializable {
  /* TODO: Think of a better state management
   * than simply inferring from last packet
  */
  public boolean isDuplicate(Message state, Message inMessage);
}
