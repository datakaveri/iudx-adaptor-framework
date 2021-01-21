package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import in.org.iudx.adaptor.datatypes.Message;

public interface Deduplicator extends Serializable {
  public boolean isDuplicate(Message inMessage);
}
