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


/* 
 * Parse input json data based on json path specifier
 **/
public class JsonPathParser<T> implements Parser<T> {

  private transient JSONObject parseSpec;

  private String timestampPath;
  private String keyPath;
  private String containerPath;

  private static final Logger LOGGER = LogManager.getLogger(JsonPathParser.class);

  public JsonPathParser(String parseSpecString) {

    this.parseSpec = new JSONObject(parseSpecString);

    try {
      this.timestampPath = parseSpec.getString("timestampPath");
      this.keyPath = parseSpec.getString("keyPath");
      this.containerPath = parseSpec.getString("containerPath");
    } catch (Exception e) {
      // TODO: Handle this
    }
  }


  public T parse(String message) {

    ReadContext ctx = JsonPath.parse(message);

    /* Parse into Message */
    if (containerPath != null && containerPath.isEmpty()) {
      Message msg = new Message();
      msg.setEventTimestamp(Instant.parse(ctx.read(timestampPath)));
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
        String key = tmpctx.read(keyPath);
        String timeString = tmpctx.read(timestampPath);
        tmpmsg.setEventTimestamp(Instant.parse(timeString));
        tmpmsg.setKey(key);
        tmpmsg.setResponseBody(tmpctx.jsonString());
        msgArray.add(tmpmsg);
      }
      return (T) msgArray;
    }
  }

  public byte[] serialize(Message obj) {
    return obj.toString().getBytes();
  }


}
