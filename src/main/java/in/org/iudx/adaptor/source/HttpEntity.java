package in.org.iudx.adaptor.source;


import java.time.Instant;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


import in.org.iudx.adaptor.datatypes.GenericJsonMessage;



/**
 * Todos:
 *  - Response: Make it closeable
 **/

public class HttpEntity {

  public Api api;
  public String serializedMessage;
  public String key;
  public Instant timestamp;

  public OkHttpClient client;
  public Response response;


  public HttpEntity(Api api) {
    this.api = api;
    client = new OkHttpClient();
  }

  public HttpEntity setApi(Api api) {
    this.api = api;
    return this;
  }

  public Api getApi() {
    return this.api;
  }


  public Response getSerializedMessage() throws IOException {
    if (api.requestType == "get") {
      Request request = new Request.Builder()
                              .url(api.url)
                              .headers(api.headers)
                              .build();
      try (Response response = client.newCall(request).execute()) {
        this.response = response;
        return response;
      }
    }
    return null;
  }


  public GenericJsonMessage getMessage() {
    /** Todo:
     *    - Parse Response into a new GenericJsonMessage object
     **/
    GenericJsonMessage msg = new GenericJsonMessage();
    return msg;
  }

}
