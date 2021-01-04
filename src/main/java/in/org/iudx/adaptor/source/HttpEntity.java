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

  public ApiConfig apiConfig;

  private HttpClient httpClient;
  private HttpRequest httpRequest;



  /**
   * {@link HttpEntity} Constructor
   *
   * @param apiConfig The apiConfig to make requests and get responses
   * 
   * Note: This is called from context open() methods of the Source Function
   *
   * Todos: 
   *  - Manage post 
   *  - Modularize/cleanup
   *  - Handle timeouts from ApiConfig
   */
  public HttpEntity(ApiConfig apiConfig) {
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

  public HttpEntity setApi(ApiConfig api) {
    this.apiConfig = api;
    return this;
  }

  public ApiConfig getApiConfig() {
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
   * Todos:
   *  - Parse propertly
   *  - Parsing logic goes here. It's a complex bit of code to write.
   */
  public GenericJsonMessage getMessage() {
    GenericJsonMessage msg = new GenericJsonMessage();
    String resp = getSerializedMessage();
    msg.key = "test1";
    msg.body = resp;
    return msg;
  }

}
