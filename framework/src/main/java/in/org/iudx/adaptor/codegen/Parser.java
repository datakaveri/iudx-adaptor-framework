package in.org.iudx.adaptor.codegen;

import java.io.Serializable;

/* PO - Parser output. Thought to handle Message vs Array<Message> */

public interface Parser<PO> extends Serializable {
  public PO parse(String data);
}
