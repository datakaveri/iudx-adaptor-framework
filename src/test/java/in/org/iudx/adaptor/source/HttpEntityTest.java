package in.org.iudx.adaptor.source;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.CheckpointConfig;


import java.util.Map;
import java.util.HashMap;

import in.org.iudx.adaptor.datatypes.GenericJsonMessage;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleTestTransformer;
import in.org.iudx.adaptor.codegen.SimpleTestParser;

public class HttpEntityTest {

  @Test
  void simpleGet() throws InterruptedException {

    
    SimpleTestTransformer trans = new SimpleTestTransformer();
    SimpleTestParser parser = new SimpleTestParser();

    ApiConfig<Parser,Transformer> apiConfig = 
      new ApiConfig<Parser,Transformer>().setUrl("http://127.0.0.1:8080/simpleA")
                                          .setRequestType("GET")
                                          .setKeyingProperty("deviceId")
                                          .setTimeIndexingProperty("time")
                                          .setPollingInterval(1000L)
                                          .setParser(parser)
                                          .setTransformer(trans);

    HttpEntity httpEntity = new HttpEntity(apiConfig);
    GenericJsonMessage msg = httpEntity.getMessage();
    System.out.println("Result");
    System.out.println(msg);
    System.out.println("Keying Property");
    System.out.println(msg.key);
    System.out.println("Time");
    System.out.println(msg.getEventTime());

  }
}
