package in.org.iudx.adaptor.utils;


import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 *  - Parse response bodies
 *
 */
public class HttpEntity {

  public ApiConfig apiConfig;

  private HttpRequest.Builder requestBuilder;
  private HttpClient httpClient;
  private HttpRequest httpRequest;

  private String body;

  private static final Logger LOGGER = LogManager.getLogger(HttpEntity.class);


  /**
   * {@link HttpEntity} Constructor
   *
   * @param apiConfig The apiConfig to make requests and get responses
   * 
   * Note: This is called from context open() methods of the Source Function
   *
   * TODO: 
   *  - Modularize/cleanup
   *  - Handle timeouts from ApiConfig
   */
  public HttpEntity(ApiConfig apiConfig) {
    this.apiConfig = apiConfig;

    requestBuilder = HttpRequest.newBuilder();

    HttpClient.Builder clientBuilder = HttpClient.newBuilder();
    clientBuilder.version(Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(10));

    if (apiConfig.url != null ) {
      requestBuilder.uri(URI.create(apiConfig.url));
    }
    /* TODO: consider making this neater */
    if (this.apiConfig.getHeaderString().length > 0) {
      requestBuilder.headers(this.apiConfig.headersArray);
    }
    httpClient = clientBuilder.build();
  }

  public HttpEntity setUrl(String url) {
    requestBuilder.uri(URI.create(url));
    return this;
  }

  public HttpEntity setBody(String body) {
    this.body = body;
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
      httpRequest = requestBuilder.build();
    }
    else if (apiConfig.requestType.equals("POST")) {
      String reqBody = "";
      if (this.body == null) {
        if (apiConfig.body == null) {
          return "";
        } else {
          reqBody = apiConfig.body;
        }
      } else {
        reqBody = apiConfig.body;
      }
      httpRequest = requestBuilder.POST(BodyPublishers.ofString(reqBody))
                                        .build();
    }
    try {
      HttpResponse<String> resp =
        httpClient.send(httpRequest, BodyHandlers.ofString());
      return resp.body();
    } catch (Exception e) {
      LOGGER.debug(e);
      return "";
    }
  }


  /*
   * TODO: 
   * - Manage non-post requests
   * - Handle auth
   */
  public String postSerializedMessage(String message) {
    httpRequest = requestBuilder.POST(BodyPublishers.ofString(message)).build();
    try {
      HttpResponse<String> resp =
        httpClient.send(httpRequest, BodyHandlers.ofString());
      return resp.body();
    } catch (Exception e) {
      return "";
    }
  }
}
