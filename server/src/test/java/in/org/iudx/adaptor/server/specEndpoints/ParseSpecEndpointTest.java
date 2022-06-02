package in.org.iudx.adaptor.server.specEndpoints;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.utils.HttpEntity;

public class ParseSpecEndpointTest {

  private static final Logger LOGGER = LogManager.getLogger(ParseSpecEndpointTest.class);

  @BeforeAll
  public static void intialize() {
  }

  @Test
  void testParseSpec() throws InterruptedException {

    JsonObject spec = new JsonObject()
      .put("timestampPath", "$.time")
      .put("messageContainer", "array")
      .put("keyPath", "$.id")
      .put("containerPath", "$.data")
      .put("inputTimeFormat","yyyy-MM-dd HH:mm:ss")
      .put("outputTimeFormat", "yyyy-MM-dd'T'HH:mm:ssXXX");

    String data = 
      new JsonObject().put("data",
      new JsonArray()
      .add(new JsonObject()
      .put("time", "2021-04-01 12:00:01")
      .put("id", "123")
      .put("k", 1.5)
      )
      .add((new JsonObject()
      .put("time", "2021-05-01 12:00:01")
      .put("id", "4356")
      .put("k", 2.5)
      ))).toString();

    ParseSpecEndpoint pse = new ParseSpecEndpoint();
    try {
      String res = pse.run(data, spec);
      LOGGER.debug(res);
    } catch (Exception e) {
      LOGGER.debug(e);
    }
  }

}
