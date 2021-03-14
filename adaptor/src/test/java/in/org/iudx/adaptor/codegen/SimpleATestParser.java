package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;

/* 
 * PO - Parser Output
 **/
public class SimpleATestParser implements Parser<Message> {

  private String key;
  private Instant time;
  private JSONObject data;

  public SimpleATestParser() {
  }


  public Message parse(String message) {
    // Try catch around this
    data = new JSONObject(message);
    Message msg = new Message();
    time = Instant.parse(data.getString("time"));
    key = data.getString("deviceId");
    msg.setKey(key);
    msg.setEventTimestamp(time);
    msg.setResponseBody(message);
    return msg;
  }

  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }


}
