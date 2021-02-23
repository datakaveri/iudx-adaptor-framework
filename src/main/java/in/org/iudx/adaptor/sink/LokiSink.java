package in.org.iudx.adaptor.sink;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.impl.StringEscapeUtils;

public class LokiSink implements SinkFunction<String> {

  private static final long serialVersionUID = 1L;  
  
  private HttpClient httpClient;
  private HttpRequest httpRequest;
  Object requestOptions;

  public LokiSink(Object object) {
    this.requestOptions = object;
  }


  @Override
  public void invoke(String value) throws Exception {

    JsonObject confJson = new JsonObject((String) requestOptions);
    JsonObject lokiPaylod = confJson.getJsonObject("lokiPaylod");

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
    HttpClient.Builder clientBuilder = HttpClient.newBuilder();
    clientBuilder.version(Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(10));
    
    requestBuilder.uri(URI.create(confJson.getString("host")));
    requestBuilder.header("content-type", "application/json");

    long timeNs = System.currentTimeMillis() * 1000000;

    String finalLog = lokiPaylod.toString().replace("$1", Long.toString(timeNs)).replace("$2",
        StringEscapeUtils.escapeJava(value));
   
    // JSONArray logData = new JSONArray().put(Long.toString(timeNs)).put(value);
    // System.out.println("LOKIVALUE: "+ logData.toString());
    // lokiPaylod.getJsonArray("streams").getJsonObject(0).getJsonArray("values").add(new
    // JSONArray().put(logData));
    
    System.out.println("LOKIPAYLOAD: " + finalLog);

    /*
     * JSONObject lokiMessage = new JSONObject().put("streams", new JSONArray() .put(new
     * JSONObject().put("stream", new JSONObject().put("flinkhttp", "test-sideoutput"))
     * .put("values", new JSONArray().put(logData))));
     */

    requestBuilder.POST(BodyPublishers.ofString(finalLog));
    httpRequest = requestBuilder.build();
    httpClient = clientBuilder.build();

    HttpResponse<String> resp =
        httpClient.send(httpRequest, BodyHandlers.ofString());
  }
}
