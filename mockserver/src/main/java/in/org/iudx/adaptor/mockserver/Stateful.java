package in.org.iudx.adaptor.mockserver;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.util.Random;




/**
 * Flows with memory
 * TODO: 
 *  - Everything
 */
public class Stateful {

  private static String HEADER_CONTENT_TYPE = "content-type";
  private static String MIME_APPLICATION_JSON = "application/json";


  private Random rand = new Random(); 

  private JsonObject simpleA = new JsonObject(
          " {\"deviceId\": \"abc-123\", \"k1\": 1, \"time\": \"\" }" );

  public Stateful() {
  }


  /**
   *
   * Packet example -
   *   {
   *       "deviceId": "<use-this as the name of the device>",
   *       "k1": "<int> <random number>",
   *       "time": "<iso-date-time> <watermark/deduplicate based on this"
   *   }
   *
   *
   * Output info -
   *  - Same id (stream key)
   *  - Random 
   *
   */
  public void getSimplePacketA(RoutingContext routingContext) {

    HttpServerResponse response = routingContext.response();

    String timeNow = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
                              .withZone(ZoneOffset.UTC)
                              .format(Instant.now());

    simpleA.put("time", timeNow);
    simpleA.put("k1", rand.nextInt(1000));

    response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON);
    response.setStatusCode(200).end(simpleA.toString());

  }
}
