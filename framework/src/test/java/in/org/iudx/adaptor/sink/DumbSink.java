package in.org.iudx.adaptor.sink;

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


  private static final Logger LOGGER = LogManager.getLogger(DumbSink.class);

  private StaticStringPublisher publisher;

  public DumbSink() {
    publisher = new StaticStringPublisher("test", "test");
  }

  /** Statefulness for dumb things */
  public void open(Configuration config) throws Exception {
  }


  /**
   * Called for every message the incoming data
   */
  @Override
  public void invoke(Message msg) {
    LOGGER.info(new String(publisher.serialize(msg)));
  }

}
