package in.org.iudx.adaptor.testadaptors;

import org.json.JSONObject;
import org.json.JSONArray;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Parser;
import java.util.List;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/* 
 * PO - Parser Output
 **/
public class SimpleBTestParser implements Parser<Message[]> {

  private String key;
  private Instant time;
  private JSONObject msg;
  private JSONArray data;


  private DateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private DateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

  private static final Logger LOGGER = LogManager.getLogger(SimpleBTestParser.class);

  public SimpleBTestParser() {
    fromFormat.setTimeZone(TimeZone.getTimeZone("IST"));
    toFormat.setTimeZone(TimeZone.getTimeZone("IST"));
  }


  public Message[] parse(String message) {
    // Try catch around this
    msg = new JSONObject(message);
    data = msg.getJSONArray("met");
    List<Message> msgArray = new ArrayList<Message>();
    for (int i=0; i<data.length(); i++){
      Message tmpmsg = new Message();
      JSONObject tmpObj = data.getJSONObject(i);
      tmpmsg.setKey(tmpObj.getString("EqpName"));
      try {
        Date date = fromFormat.parse(tmpObj.getString("Date"));
        tmpmsg.setEventTimeAsString(toFormat.format(date));
        tmpmsg.setEventTimestamp(date.toInstant());
      } catch (Exception e) {
        continue;
      }
      tmpmsg.setResponseBody(tmpObj.toString());
      msgArray.add(tmpmsg);
    }
    return msgArray.toArray(new Message[0]);
  }

  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }


}
