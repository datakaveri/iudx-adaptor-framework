package in.org.iudx.adaptor.sink;

import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import in.org.iudx.adaptor.utils.HttpEntity;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleStringParser;
import org.apache.flink.configuration.Configuration;

public class HttpSink extends RichSinkFunction<String> {

  private static final long serialVersionUID = 54L;  
  
  private HttpEntity<String> httpEntity;
  private ApiConfig apiConfig;
  private SimpleStringParser parser;


  public HttpSink(ApiConfig apiConfig) {
    this.apiConfig = apiConfig;
    parser = new SimpleStringParser();
  }

  /**
   * Retrieve stateful context info
   * 
   * @param Configuration Flink managed state configuration
   *
   * Note: 
   *   - This is where {@link HttpEntity} must be initialized
   */
  @Override
  public void open(Configuration config) throws Exception {
    super.open(config);
    httpEntity = new HttpEntity<String>(apiConfig, parser);
  }

  /* TODO: handle different http methods */
  @Override
  public void invoke(String message) throws Exception {
    httpEntity.postSerializedMessage(message);

  }
}
