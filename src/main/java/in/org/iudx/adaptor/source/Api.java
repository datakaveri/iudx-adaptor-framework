package in.org.iudx.adaptor.source;

import okhttp3.Headers;
import okhttp3.RequestBody;
import okhttp3.HttpUrl;


public class Api {
  HttpUrl url;
  Headers headers;
  RequestBody body;
  String requestType;

  public Api(HttpUrl url, Headers headers){
    this.url = url;
    this.headers = headers;
  }

  public Api setBody(RequestBody body) {
    this.body = body;
    return this;
  }

  public Api setHeaders(Headers headers) {
    this.headers = headers;
    return this;
  }

  /* type is get or post */
  public Api setRequestType(String type) {
    this.requestType = type;
    return this;
  }

}
