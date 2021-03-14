package in.org.iudx.adaptor.codegen;

import in.org.iudx.adaptor.datatypes.Message;


public class SimplePublisher implements Publisher {

  public SimplePublisher() {
  }

  public String computeMessageTag(Message msg) {
    return "test";
  }

  public String computeSinkName(Message msg) {
    return "adaptor-test";
  }
}
