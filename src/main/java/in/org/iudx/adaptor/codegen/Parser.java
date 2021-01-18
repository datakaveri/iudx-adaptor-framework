package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;

public interface Parser extends Serializable {
  public String getKey();
  public Instant getTimeIndex();
  public Message parse(String data);
}
