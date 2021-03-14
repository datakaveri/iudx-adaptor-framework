package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import in.org.iudx.adaptor.datatypes.Message;

public interface Transformer extends Serializable {
  public Message transform(Message inMessage) throws Exception;
}
