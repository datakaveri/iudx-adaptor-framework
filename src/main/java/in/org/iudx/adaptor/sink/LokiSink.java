package in.org.iudx.adaptor.sink;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.impl.StringEscapeUtils;

public class LokiSink implements SinkFunction<String> {

  private static final long serialVersionUID = 1L;
  CloseableHttpClient client;
  HttpPost request;
  StringEntity requestEntity;
  Object requestOptions;

  public LokiSink(Object object) {
    this.requestOptions = object;
  }


  @Override
  public void invoke(String value) throws Exception {

    JsonObject confJson = new JsonObject((String) requestOptions);
    JsonObject lokiPaylod = confJson.getJsonObject("lokiPaylod");

    client = HttpClients.createDefault();
    request = new HttpPost(confJson.getString("host"));
    request.addHeader("content-type", "application/json");

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


    requestEntity = new StringEntity(finalLog, ContentType.APPLICATION_JSON);
    request.setEntity(requestEntity);

    try {

      CloseableHttpResponse response = client.execute(request);
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
