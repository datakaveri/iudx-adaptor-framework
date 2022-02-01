package in.org.iudx.adaptor.source;

import org.json.JSONObject;
import org.json.JSONArray;
import java.time.Instant;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Parser;
import com.jayway.jsonpath.JsonPath;
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
  private String defaultTimePath = "observationDateTime";

  private String inputTimeFormat;
  private String outputTimeFormat;

  private DateFormat fromFormat;
  private DateFormat toFormat;

  private boolean hasTrickleKeys;

  private static final Logger LOGGER = LogManager.getLogger(JsonPathParser.class);

  public JsonPathParser(String parseSpecString) {

    this.parseSpec = new JSONObject(parseSpecString);


    try {
      this.timestampPath = parseSpec.optString("timestampPath");
      this.keyPath = parseSpec.getString("keyPath");

      this.containerPath = parseSpec.optString("containerPath");
      this.inputTimeFormat = parseSpec.optString("inputTimeFormat");
      this.outputTimeFormat = parseSpec.optString("outputTimeFormat");
      // Default
      toFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    } catch (Exception e) {
      LOGGER.error("Unable to process with JsonPathParser");
      // TODO: Exit
    }

    if (!inputTimeFormat.isEmpty()) {
     fromFormat = new SimpleDateFormat(inputTimeFormat); 
     fromFormat.setTimeZone(TimeZone.getTimeZone("IST"));
    }

    if(!outputTimeFormat.isEmpty()) {
     toFormat = new SimpleDateFormat(outputTimeFormat); 
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



  public Message parseTime(DocumentContext ctx, Message outMsg) {
    String message = ctx.jsonString();
    if (timestampPath.isEmpty() || inputTimeFormat.isEmpty()) {
      Date now = new Date();
      String parsedDate = toFormat.format(now);

      ctx.put("$", defaultTimePath, parsedDate);

      message = ctx.jsonString();

      outMsg.setEventTimeAsString(parsedDate);
      outMsg.setEventTimestamp(now.toInstant());
      outMsg.setResponseBody(message);
    } 
    else if  
      (!timestampPath.isEmpty() 
       && !inputTimeFormat.isEmpty() 
       && outputTimeFormat.isEmpty()) {
        outMsg.setEventTimeAsString(ctx.read(timestampPath));
        outMsg.setEventTimestamp(Instant.parse(ctx.read(timestampPath)));
        outMsg.setResponseBody(message);
      }
    else {
      try {
        Date date = fromFormat.parse(ctx.read(timestampPath));
        String parsedDate = toFormat.format(date);
        outMsg.setEventTimeAsString(parsedDate);
        outMsg.setEventTimestamp(date.toInstant());
        message = JsonPath.parse(message).set(timestampPath, parsedDate).jsonString();
        outMsg.setResponseBody(message);
      } catch (Exception e) {
        LOGGER.debug(e);
        // TODO: Handle this
      }
    }
    return outMsg;
  }

  /* TODO:
   *  - Abstract out parsing time
   */
  public T parse(String message) {

    DocumentContext ctx = JsonPath.parse(message);

    /* Parse into Message */
    if (containerPath.isEmpty()) {
      Message msg = new Message();
      msg.setKey(ctx.read(keyPath).toString());
      msg = parseTime(ctx, msg);
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
                ctx.read(trickleObjs.getJSONObject(j).getString("keyPath").toString());
              tmpctx.put("$",
                          trickleObjs.getJSONObject(j).getString("keyName"),
                          keyval);
            } catch (Exception e) {
              LOGGER.debug(e);
              // Ignore errors
            }
          }
        }

        String key = tmpctx.read(keyPath).toString();
        tmpmsg = parseTime(tmpctx, tmpmsg);
        tmpmsg.setKey(key);
        msgArray.add(tmpmsg);
      }
      return (T) msgArray;
    }
  }

  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }


}
