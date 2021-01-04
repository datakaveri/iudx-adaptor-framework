package in.org.iudx.adaptor.source;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;
import java.lang.StringBuilder;


/**
 * {@link ApiConfig} - Api configuration class
 * Encapsulates api information such as making a url,
 * defining request type, adding headers etc.
 * Note: This must be serializable since flink passes
 * this accross its instances.
 *
 * Todos: 
 *  - Extend as needed
 *
 */
public class ApiConfig implements Serializable {
  public String url;
  public String body;
  public String requestType = "GET";

  public Map<String,String> headers = new HashMap<String,String>();
  private String headerString = "";
  private static final long serialVersionUID = 2L;

  public ApiConfig(){
  }

  public ApiConfig setUrl(String url) {
    this.url = url;
    return this;
  }

  public ApiConfig setBody(String body) {
    this.body = body;
    return this;
  }

  public ApiConfig setHeader(String key, String value) {
    StringBuilder sb = new StringBuilder();
    sb.append(headers);
    sb.append(key).append(" ").append(value);
    headerString = sb.toString();
    return this;
  }

  public ApiConfig setHeaders(Map<String,String> headers) {
    this.headers = headers;
    StringBuilder sb = new StringBuilder();
    headers.forEach((k,v) -> {
      sb.append(k).append(" ").append(v);
    });
    headerString = sb.toString();
    return this;
  }

  /* type is get or post */
  public ApiConfig setRequestType(String requestType) {
    this.requestType = requestType;
    return this;
  }

  public String getHeaderString() {
    return headerString;
  }

}
