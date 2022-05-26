package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.configuration.Configuration;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.org.iudx.adaptor.codegen.Parser;
import org.apache.flink.api.common.serialization.SerializationSchema;

/** DumbSink
 *  A dumb sink which prints the DataStream
 **/
public class DumbSink implements SinkFunction<Message> {


  static CustomLogger logger;

  private StaticStringPublisher publisher;

  public DumbSink() {
    publisher = new StaticStringPublisher("test", "test");
    logger = (CustomLogger) CustomLogger.getLogger(DumbSink.class, "unit_test");
  }

  /** Statefulness for dumb things */
  public void open(Configuration config) throws Exception {
  }


  /**
   * Called for every message the incoming data
   */
  @Override
  public void invoke(Message msg) {
    System.out.println(new String(publisher.serialize(msg)));
    logger.info(new String(publisher.serialize(msg)));
  }

}
