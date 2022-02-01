package in.org.iudx.adaptor.utils;

import java.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;
import in.org.iudx.adaptor.codegen.ApiConfig;

public class HttpEntityTest {
  
  private static final Logger LOGGER = LogManager.getLogger(HttpEntityTest.class);
  
  @Test
  void simpleGet() throws InterruptedException {

    

    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8080/simpleA")
                                          .setRequestType("GET")
                                          .setPollingInterval(1000L);

    HttpEntity httpEntity = new HttpEntity(apiConfig);
    LOGGER.info("SimpleGet :"+httpEntity.getSerializedMessage());
  }
  
  @Test
  void validAuthGet() {
    
    String basicAuthStr = Base64.getEncoder().encodeToString(("admin:admin").getBytes());
    
    ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8080/auth/simpleA")
                                         .setRequestType("GET")
                                         .setPollingInterval(1000)
                                         .setHeader("Authorization","Basic "+ basicAuthStr);
    
    HttpEntity httpEntity = new HttpEntity(apiConfig);
    LOGGER.info("ValidAuthSimpleGet :"+httpEntity.getSerializedMessage());
  }
  
  @Test
  void invalidAuthGet() {
    
    String basicAuthStr = Base64.getEncoder().encodeToString(("admin1:admin").getBytes());
    
    ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8080/auth/simpleA")
                                         .setRequestType("GET")
                                         .setHeader("Authorization","Basic "+ basicAuthStr);
    
    HttpEntity httpEntity = new HttpEntity(apiConfig);
    LOGGER.error("invalidAuthSimpleGet :"+httpEntity.getSerializedMessage());
  }

}
