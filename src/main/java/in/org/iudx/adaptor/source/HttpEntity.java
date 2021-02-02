package in.org.iudx.adaptor.source;


import java.time.Instant;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.net.URI;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.codegen.ApiConfig;

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
   * TODO: 
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

  public HttpEntity setApi(ApiConfig apiConfig) {
    this.apiConfig = apiConfig;
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


  public Message getMessage() {
    Message msg = apiConfig.parser.parse(getSerializedMessage());
    return msg;
  }
}
