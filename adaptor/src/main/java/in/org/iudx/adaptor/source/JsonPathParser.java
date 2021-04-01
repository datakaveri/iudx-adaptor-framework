package in.org.iudx.adaptor.source;

import org.json.JSONObject;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Parser;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.List;
import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

/* 
 * Parse input json data based on json path specifier
 **/
public class JsonPathParser<T> implements Parser<T> {

  private transient JSONObject parseSpec;

  private String timestampPath;
  private String keyPath;
  private String containerPath;

  private String inputTimeFormat;
  private String outputTimeFormat;

  private DateFormat fromFormat;
  private DateFormat toFormat;


  private static final Logger LOGGER = LogManager.getLogger(JsonPathParser.class);

  public JsonPathParser(String parseSpecString) {

    this.parseSpec = new JSONObject(parseSpecString);

    try {
      this.timestampPath = parseSpec.getString("timestampPath");
      this.keyPath = parseSpec.getString("keyPath");

      this.containerPath = parseSpec.optString("containerPath");
      this.inputTimeFormat = parseSpec.optString("inputTimeFormat");
      this.outputTimeFormat = parseSpec.optString("outputTimeFormat");

    } catch (Exception e) {
      // TODO: Exit
    }

    if (!inputTimeFormat.isEmpty() && !outputTimeFormat.isEmpty()) {
     fromFormat = new SimpleDateFormat(inputTimeFormat); 
     toFormat = new SimpleDateFormat(outputTimeFormat); 
     fromFormat.setTimeZone(TimeZone.getTimeZone("IST"));
     toFormat.setTimeZone(TimeZone.getTimeZone("IST"));
      
    }
  }


  public T parse(String message) {

    ReadContext ctx = JsonPath.parse(message);

    /* Parse into Message */
    if (containerPath.isEmpty()) {
      Message msg = new Message();
      try {
        Date date = fromFormat.parse(ctx.read(timestampPath));
        String parsedDate = toFormat.format(date);
        msg.setEventTimeAsString(parsedDate);
        msg.setEventTimestamp(date.toInstant());
        message = JsonPath.parse(message).set(timestampPath, parsedDate).jsonString();
      } catch (Exception e) {
        // TODO: Handle this
      }
      msg.setKey(ctx.read(keyPath));
      msg.setResponseBody(message);
      return (T) msg;
    }
    else {
      List<Object> container = ctx.read(containerPath);
      List<Message> msgArray = new ArrayList<Message>();
      for (int i=0; i<container.size(); i++){
        // TODO: Improve
        Message tmpmsg = new Message();
        ReadContext tmpctx = JsonPath.parse(container.get(i));
        String msgBody = tmpctx.jsonString();
        String key = tmpctx.read(keyPath);
        try {
          Date date = fromFormat.parse(tmpctx.read(timestampPath));
          String parsedDate = toFormat.format(date);
          tmpmsg.setEventTimeAsString(parsedDate);
          tmpmsg.setEventTimestamp(date.toInstant());
          msgBody = JsonPath.parse(msgBody).set(timestampPath, parsedDate).jsonString();
        } catch (Exception e) {
          // TODO: Handle this
        }
        tmpmsg.setKey(key);
        tmpmsg.setResponseBody(msgBody);
        msgArray.add(tmpmsg);
      }
      return (T) msgArray;
    }
  }

  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }


}
