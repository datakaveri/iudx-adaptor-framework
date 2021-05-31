package in.org.iudx.adaptor.source;

import org.json.JSONObject;
import org.json.JSONArray;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Parser;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.DocumentContext;
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

  private String trickleObjsString;
  private String timestampPath;
  private String keyPath;
  private String containerPath;

  private String inputTimeFormat;
  private String outputTimeFormat;

  private DateFormat fromFormat;
  private DateFormat toFormat;

  private boolean hasTrickleKeys;

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
      LOGGER.error("Unable to process with JsonPathParser");
      // TODO: Exit
    }

    if (!inputTimeFormat.isEmpty() && !outputTimeFormat.isEmpty()) {
     fromFormat = new SimpleDateFormat(inputTimeFormat); 
     toFormat = new SimpleDateFormat(outputTimeFormat); 
     fromFormat.setTimeZone(TimeZone.getTimeZone("IST"));
     toFormat.setTimeZone(TimeZone.getTimeZone("IST"));
    }

    initialize();

  }

  // TODO: Improve, currently wasteful
  public JsonPathParser<T> initialize() {
    // Trickle paths
    if(parseSpec.has("trickle")) {
      hasTrickleKeys = true;
      trickleObjsString = parseSpec.getJSONArray("trickle").toString();
    } else {
      hasTrickleKeys = false;
    }
    return this;
  }


  /* TODO:
   *  - Abstract out parsing time
   */
  public T parse(String message) {

    ReadContext ctx = JsonPath.parse(message);

    /* Parse into Message */
    if (containerPath.isEmpty()) {
      Message msg = new Message();
      msg.setKey(ctx.read(keyPath));
      msg.setResponseBody(message);
      if (inputTimeFormat.isEmpty() || outputTimeFormat.isEmpty()) {
        msg.setEventTimeAsString(ctx.read(timestampPath));
        msg.setEventTimestamp(Instant.parse(ctx.read(timestampPath)));
      }
      else {
        try {
          Date date = fromFormat.parse(ctx.read(timestampPath));
          String parsedDate = toFormat.format(date);
          msg.setEventTimeAsString(parsedDate);
          msg.setEventTimestamp(date.toInstant());
          message = JsonPath.parse(message).set(timestampPath, parsedDate).jsonString();
        } catch (Exception e) {
          // TODO: Handle this
        }
      }
      return (T) msg;
    }
    else {


      List<Object> container = ctx.read(containerPath);
      List<Message> msgArray = new ArrayList<Message>();

      JSONArray trickleObjs = new JSONArray();

      if (hasTrickleKeys == true) {
        trickleObjs = new JSONArray(trickleObjsString);
      }

      for (int i=0; i<container.size(); i++){
        // TODO: Improve
        Message tmpmsg = new Message();
        DocumentContext tmpctx = JsonPath.parse(container.get(i));

        // Add the trickle keys
        // TODO: This is pretty inefficient
        if (hasTrickleKeys == true) {
          for (int j=0; j<trickleObjs.length(); j++) {
            try {
              Object keyval =
                ctx.read(trickleObjs.getJSONObject(j).getString("keyPath"));
              tmpctx.put("$", trickleObjs.getJSONObject(j).getString("keyName"), keyval);
            } catch (Exception e) {
              LOGGER.debug(e);
              // Ignore errors
            }
          }
        }


        String msgBody = tmpctx.jsonString();
        String key = tmpctx.read(keyPath);




        if (inputTimeFormat.isEmpty() || outputTimeFormat.isEmpty()) {
          tmpmsg.setEventTimeAsString(tmpctx.read(timestampPath));
          tmpmsg.setEventTimestamp(Instant.parse(tmpctx.read(timestampPath)));
        }
        else {
          try {
            Date date = fromFormat.parse(tmpctx.read(timestampPath));
            String parsedDate = toFormat.format(date);
            tmpmsg.setEventTimeAsString(parsedDate);
            tmpmsg.setEventTimestamp(date.toInstant());
            msgBody = JsonPath.parse(msgBody).set(timestampPath, parsedDate).jsonString();
          } catch (Exception e) {
            // TODO: Handle this
          }
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
