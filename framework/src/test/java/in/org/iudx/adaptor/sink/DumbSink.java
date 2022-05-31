package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.configuration.Configuration;

/** DumbSink
 *  A dumb sink which prints the DataStream
 **/
public class DumbSink implements SinkFunction<Message> {


  static CustomLogger logger;

  private StaticStringPublisher publisher;

  public DumbSink() {
    publisher = new StaticStringPublisher("test", "test");
    logger = new CustomLogger(DumbSink.class, "unit_test");
  }

  /** Statefulness for dumb things */
  public void open(Configuration config) throws Exception {
  }


  /**
   * Called for every message the incoming data
   */
  @Override
  public void invoke(Message msg, Context ctx) {
    logger.info(new String(publisher.serialize(msg)));
  }
}
