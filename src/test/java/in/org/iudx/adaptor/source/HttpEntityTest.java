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

public class HttpEntityTest {

  @Test
  void simpleGet() throws InterruptedException {

    ApiConfig apiConfig = new ApiConfig().setUrl("http://127.0.0.1:8080/simpleA")
                                          .setRequestType("GET")
                                          .setKeyingProperty("deviceId")
                                          .setTimeIndexingProperty("time");

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
