package in.org.iudx.adaptor.mockserver;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.Random;




/**
 * Simple Flows
 * TODO: 
 *  - Simple http requests involving simple http responses.
 */
public class Complex {

  private static String HEADER_CONTENT_TYPE = "content-type";
  private static String MIME_APPLICATION_JSON = "application/json";

  private int countA = 1;
  private int countB = 1;
  private int duplicateB = 0;
  private int DUPLICATE_EVERY = 1;

  private Random rand = new Random(); 

  private JsonObject combineA = new JsonObject("{ \"time\": \"123\", \"outerkey\": \"outerkeyval\", \"data\": [] }");

  public Complex() {
  }



  /**
   *
   * Packet example -
   *   {
   *       "time": "2021....",
   *       "outerkey": "outerkeyval",
   *       "data": [
   *        {
   *       "deviceId": "<use-this as the name of the device>",
   *       "k1": "abc",
   *       "k2": "123",
   *       "k3": null,
   *       "k4": "abc_reject_me"
   *      }
   *     ]
   *   }
   *
   *
   * Output info -
   *  - Same id (stream key)
   *  - Random 
   *
   */
  public void getCombineA(RoutingContext routingContext) {

    HttpServerResponse response = routingContext.response();


    String t = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")
      .withZone(ZoneOffset.UTC)
      .format(Instant.now().minusSeconds(10L));

    combineA.put("time", t);

    JsonArray arr = new JsonArray();


    for (int i=0; i<10; i++) {
      JsonObject tmp = new JsonObject();
      tmp.put("k1", "abc");
      tmp.put("k2", "123");
      tmp.put("k3", null);
      tmp.put("time", t);
      if (countB > 0) {
        tmp.put("deviceId", "abc-123");
        tmp.put("k4", "abc_accept_me");
      } else {
        tmp.put("deviceId", "abc-456");
        tmp.put("k4", "abc_reject_me");
      }

      if (++duplicateB % DUPLICATE_EVERY == 0) {
        duplicateB = 0;
        arr.add(tmp);
      }
      arr.add(tmp);
      countB *= -1;
    }
    combineA.put("data", arr);


    response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON);
    response.setStatusCode(200).end(combineA.toString());

  }
}
