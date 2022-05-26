package in.org.iudx.adaptor.sink;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import in.org.iudx.adaptor.utils.HttpEntity;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleStringParser;
import org.apache.flink.configuration.Configuration;

public class HttpSink extends RichSinkFunction<String> {

  private static final long serialVersionUID = 54L;  
  
  private HttpEntity httpEntity;
  private ApiConfig apiConfig;


  public HttpSink(ApiConfig apiConfig) {
    this.apiConfig = apiConfig;
  }

  /**
   * Retrieve stateful context info
   * 
   * @param config Flink managed state configuration
   *
   * Note: 
   *   - This is where {@link HttpEntity} must be initialized
   */
  @Override
  public void open(Configuration config) throws Exception {
    super.open(config);
    ParameterTool parameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
    String appName = parameters.getRequired("appName");
    httpEntity = new HttpEntity(apiConfig, appName);
  }

  /* TODO: handle different http methods */
  @Override
  public void invoke(String message) throws Exception {
    httpEntity.postSerializedMessage(message);

  }
}
