package in.org.iudx.adaptor.codegen;

import org.json.JSONObject;
import org.json.JSONArray;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;
import java.util.List;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* 
 * PO - Parser Output
 **/
public class SimpleBTestParser implements Parser<Message[]> {

  private String key;
  private Instant time;
  private JSONObject msg;
  private JSONArray data;

  private static final Logger LOGGER = LogManager.getLogger(SimpleBTestParser.class);

  public SimpleBTestParser() {
  }


  public Message[] parse(String message) {
    // Try catch around this
    msg = new JSONObject(message);
    data = msg.getJSONArray("data");
    List<Message> msgArray = new ArrayList<Message>();
    for (int i=0; i<data.length(); i++){
      Message tmpmsg = new Message();
      JSONObject tmpObj = data.getJSONObject(i);
      tmpmsg.setKey(tmpObj.getString("deviceId"));
      tmpmsg.setEventTimestamp(Instant.parse(tmpObj.getString("time")));
      tmpmsg.setResponseBody(tmpObj.toString());
      msgArray.add(tmpmsg);
    }
    return msgArray.toArray(new Message[0]);
  }

  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }


}
