package in.org.iudx.adaptor.codegen;

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
 * TODO: 
 *  - Extend as needed
 *
 */
public class ApiConfig<Parser,Deduplicator,Transformer> implements Serializable {
  public String url;
  public String body;
  public String requestType = "GET";
  public String keyingProperty;
  public String timeIndexingProperty;
  public long pollingInterval;
  public Parser parser;
  public Transformer transformer;
  public Deduplicator deduplicator;


  public Map<String,String> headers = new HashMap<String,String>();
  private String headerString = "";
  private static final long serialVersionUID = 2L;

  public ApiConfig(){
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setUrl(String url) {
    this.url = url;
    return this;
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setBody(String body) {
    this.body = body;
    return this;
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setHeader(String key, String value) {
    StringBuilder sb = new StringBuilder();
    sb.append(headers);
    sb.append(key).append(" ").append(value);
    headerString = sb.toString();
    return this;
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setHeaders(Map<String,String> headers) {
    this.headers = headers;
    StringBuilder sb = new StringBuilder();
    headers.forEach((k,v) -> {
      sb.append(k).append(" ").append(v);
    });
    headerString = sb.toString();
    return this;
  }


  /** TODO: This is where the keying field is described.
   *        We need to work out a mechanism to describe it through ApiConfig.
   *        For now we are just assuming a flat Json and describing the keying field
   *        as a simple string.
   **/
  public ApiConfig<Parser,Deduplicator,Transformer> setKeyingProperty(String keyingProperty) {
    this.keyingProperty = keyingProperty;
    return this;
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setTimeIndexingProperty(String timeIndexingProperty) {
    this.timeIndexingProperty = timeIndexingProperty;
    return this;
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setPollingInterval(long pollingInterval) {
    this.pollingInterval = pollingInterval;
    return this;
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setParser(Parser parser) {
    this.parser = parser;
    return this;
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setTransformer(Transformer transformer) {
    this.transformer = transformer;
    return this;
  }

  public ApiConfig<Parser,Deduplicator,Transformer> setDeduplicator(Deduplicator deduplicator) {
    this.deduplicator = deduplicator;
    return this;
  }


  /* type is get or post */
  public ApiConfig<Parser,Deduplicator,Transformer> setRequestType(String requestType) {
    this.requestType = requestType;
    return this;
  }

  public String getHeaderString() {
    return headerString;
  }

  public String getKeyingProperty() {
    return keyingProperty;
  }

  public String getTimeIndexingProperty() {
    return timeIndexingProperty;
  }

}
