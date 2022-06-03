package in.org.iudx.adaptor.server.specEndpoints;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;
import java.util.List;

import in.org.iudx.adaptor.source.JsonPathParser;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class ParseSpecEndpoint {

  private static final Logger LOGGER = LogManager.getLogger(ParseSpecEndpoint.class);

  private JsonPathParser parser;

  public ParseSpecEndpoint() {
  }

  public String run(String inputData, JsonObject spec) {
    String messageContainer = spec.getString("messageContainer");
    if (messageContainer.equals("array")) {
      parser = new JsonPathParser<List<Message>>(spec.toString());
    } else {
      parser = new JsonPathParser<Message>(spec.toString());
    }
    try {
      String msg = parser.parse(inputData).toString();
      return msg;
    } catch (Exception e) {
      return e.getMessage();
    }
  }
}
