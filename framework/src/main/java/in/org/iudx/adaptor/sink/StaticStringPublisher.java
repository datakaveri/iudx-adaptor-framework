package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.codegen.Publisher;
import in.org.iudx.adaptor.datatypes.Message;


public class StaticStringPublisher implements Publisher {

  private String sinkName;
  private String messageTag;

  public StaticStringPublisher(String sinkName, String messageTag) {
    this.sinkName = sinkName;
    this.messageTag = messageTag;
  }

  public String computeMessageTag(Message msg) {
    return messageTag;
  }

  public String computeSinkName(Message msg) {
    return sinkName;
  }

  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }
}
