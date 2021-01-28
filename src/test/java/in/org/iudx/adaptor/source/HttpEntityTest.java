package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleTestTransformer;
import in.org.iudx.adaptor.codegen.SimpleTestParser;

public class HttpEntityTest {

  @Test
  void simpleGet() throws InterruptedException {

    
    SimpleTestTransformer trans = new SimpleTestTransformer();
    SimpleTestParser parser = new SimpleTestParser();

    ApiConfig<Parser,Deduplicator,Transformer> apiConfig = 
      new ApiConfig<Parser,Deduplicator,Transformer>().setUrl("http://127.0.0.1:8080/simpleA")
                                          .setRequestType("GET")
                                          .setKeyingProperty("deviceId")
                                          .setTimeIndexingProperty("time")
                                          .setPollingInterval(1000L)
                                          .setParser(parser)
                                          .setTransformer(trans);

    HttpEntity httpEntity = new HttpEntity(apiConfig);
    Message msg = httpEntity.getMessage();
    System.out.println("Result");
    System.out.println(msg);
    System.out.println("Keying Property");
    System.out.println(msg.key);
    System.out.println("Time");
    System.out.println(msg.getEventTime());

  }
}
