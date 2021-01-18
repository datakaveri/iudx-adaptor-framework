package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;

public class SimpleTestParser implements Parser {

  private String key;
  private Instant time;
  private JSONObject data;

  public SimpleTestParser() {
  }

  public String getKey() {
    return key;
  }

  public Instant getTimeIndex() {
    return time;
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


}
