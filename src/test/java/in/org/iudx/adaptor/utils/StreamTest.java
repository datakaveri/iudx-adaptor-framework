package in.org.iudx.adaptor.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import java.util.stream.Stream;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.SimpleATestParser;


public class StreamTest {

  @Test
  void stream() throws InterruptedException {


    EndSubscriber<Message> sub = new EndSubscriber<>();
    Streamer s = new Streamer(sub);

    
  }
}
