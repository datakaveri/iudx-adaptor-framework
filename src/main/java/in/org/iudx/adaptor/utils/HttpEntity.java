package in.org.iudx.adaptor.utils;


import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Parser;

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
public class HttpEntity<PO> {

  public ApiConfig apiConfig;
  public Parser<PO> parser;

  private HttpRequest.Builder requestBuilder;
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
  public HttpEntity(ApiConfig apiConfig, Parser<PO> parser) {
    this.apiConfig = apiConfig;
    this.parser = parser;

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

    httpRequest = requestBuilder.build();
    switch (this.apiConfig.requestType) {
      case "GET": {
        try {
          HttpResponse<String> resp =
            httpClient.send(httpRequest, BodyHandlers.ofString());
          return resp.body();
        } catch (Exception e) {
          return "";
        }
      }

      case "POST":

        break;

      default:
        break;
    }



    return "";
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



  public PO getMessage() {
    /* TODO: 
     * - Make this generic, because of json array decimation 
     * - Handle arrays vs simple objects
     **/
    PO msg = parser.parse(getSerializedMessage());
    return msg;
  }
}
