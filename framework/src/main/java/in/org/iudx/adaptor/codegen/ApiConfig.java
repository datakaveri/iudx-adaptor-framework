package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;



/**
 * {@link ApiConfig} - Api configuration class
 * Encapsulates api information such as making a url,
 * defining request type, adding headers etc.
 * Note: This must be serializable since flink passes
 * this accross its instances.
 *
 * TODO: 
 *  - Extend as needed
 *  - Jackson databind
 *
 */
public class ApiConfig implements Serializable {
  public String url;
  public String body;
  public String requestType = "GET";
  public String keyingProperty;
  public String timeIndexingProperty;
  public long pollingInterval;
  public String[] headersArray;


  public Map<String,String> headers = new HashMap<String,String>();

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
    headers.put(key, value);
    return this;
  }

  public ApiConfig setPollingInterval(long pollingInterval) {
    this.pollingInterval = pollingInterval;
    return this;
  }

  /* type is get or post */
  public ApiConfig setRequestType(String requestType) {
    this.requestType = requestType;
    return this;
  }

  public String[] getHeaderString() {
    List<String> headerList = new ArrayList<String>();
    headers.forEach((k,v) -> {
      headerList.add(k);
      headerList.add(v);
    });
    headersArray = headerList.toArray(new String[0]);
    return headersArray;
  }

  public ApiConfig buildConfig() {
    return this;
  }

}
