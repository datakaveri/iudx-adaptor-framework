package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleATestParser;
import in.org.iudx.adaptor.utils.HttpEntity;

public class HttpEntityTest {

  @Test
  void simpleGet() throws InterruptedException {

    
    SimpleATestParser parser = new SimpleATestParser();

    ApiConfig apiConfig = 
      new ApiConfig().setUrl("http://127.0.0.1:8888/simpleA")
                                          .setRequestType("GET")
                                          .setPollingInterval(1000L);

    HttpEntity<Message> httpEntity = new HttpEntity<Message>(apiConfig, parser);
    Message msg = httpEntity.getMessage();
    System.out.println("Result");
    System.out.println(msg);
    System.out.println("Keying Property");
    System.out.println(msg.key);
    System.out.println("Time");
    System.out.println(msg.getEventTime());

  }
}
