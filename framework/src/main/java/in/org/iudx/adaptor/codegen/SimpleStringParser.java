package in.org.iudx.adaptor.codegen;

import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;

/* 
 * This class is a simple implementation of parser for use cases
 * where a parser is required for e.g LokiSink
 **/
public class SimpleStringParser implements Parser<String> {


  public SimpleStringParser() {
  }

  public SimpleStringParser initialize() {
    return this;
  }


  public String parse(String message) {
    // Try catch around this
    return message;
  }

  /* Unused */
  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }


}
