package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.codegen.PublishSerializer;
import in.org.iudx.adaptor.datatypes.Message;


public class JsonSerializer implements PublishSerializer {

  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }
}
