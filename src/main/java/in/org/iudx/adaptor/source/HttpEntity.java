package in.org.iudx.adaptor.source;


import java.time.Instant;
import java.io.IOException;
import org.json.JSONObject;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.net.URI;

import in.org.iudx.adaptor.datatypes.GenericJsonMessage;
import in.org.iudx.adaptor.codegen.Tagger;
import in.org.iudx.adaptor.codegen.Transformer;

/**
 * {@link HttpEntity} - Http requests/response handler
 * This class handles all http requests.
 *
 * Note: 
 *  - ?This is serializable from examples encountered. 
 *
 *
 * Todos: 
 *  - Make connection/etc closeable
 *  - Configurable timeouts
 *  - Handle post requests
 *  - Parse response bodies
 *
 */
public class HttpEntity {

  public ApiConfig<Tagger,Transformer> apiConfig;

  private HttpClient httpClient;
  private HttpRequest httpRequest;



  /**
   * {@link HttpEntity} Constructor
   *
   * @param apiConfig The apiConfig to make requests and get responses
   * 
   * Note: This is called from context open() methods of the Source Function
   *
   * TODO: 
   *  - Manage post 
   *  - Modularize/cleanup
   *  - Handle timeouts from ApiConfig<Tagger,Transformer>
   */
  public HttpEntity(ApiConfig<Tagger,Transformer> apiConfig) {
    this.apiConfig = apiConfig;
    if (this.apiConfig.requestType.equals("GET")) {
      httpRequest = HttpRequest.newBuilder().uri(URI.create(apiConfig.url))
                                    .build();
      httpClient = HttpClient.newBuilder()
                              .version(Version.HTTP_1_1)
                              .connectTimeout(Duration.ofSeconds(10))
                              .build();
                                    
    }
  }

  public HttpEntity setApi(ApiConfig<Tagger,Transformer> apiConfig) {
    this.apiConfig = apiConfig;
    return this;
  }

  public ApiConfig<Tagger,Transformer> getApiConfig() {
    return this.apiConfig;
  }


  /**
   * Get the response message as a string
   * 
   * Note:
   *  - This is the method which deals with responses Raw
   */
  public String getSerializedMessage() {
    if (apiConfig.requestType.equals("GET")) {
      try {
        HttpResponse<String> resp = httpClient.send(httpRequest, BodyHandlers.ofString());
        return resp.body();
      } catch (Exception e) {
        return "";
      }
    }
    return "";
  }


  /**
   * Get the response message parsed into {@link GenericJsonMessage}
   * 
   * TODO:
   *  - Parse propertly
   *  - Parsing logic goes here. It's a complex bit of code to write.
   */
  public GenericJsonMessage getMessage() {
    GenericJsonMessage msg = new GenericJsonMessage();
    String resp = getSerializedMessage();

    JSONObject obj = new JSONObject(resp);
    msg.setKey(apiConfig.tagger.getKey(obj));
    msg.setEventTimestamp(apiConfig.tagger.getTimeIndex(obj));
    msg.setResponseBody(resp);
    return msg;
  }

}
