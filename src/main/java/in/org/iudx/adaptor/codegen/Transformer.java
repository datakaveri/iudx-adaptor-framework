package in.org.iudx.adaptor.codegen;

import java.io.Serializable;

public interface Transformer extends Serializable {
  public String transform(String inDoc);
}
