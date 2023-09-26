package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * DumbSink
 * A dumb sink which prints the DataStream
 **/
public class DumbSink implements SinkFunction<Message> {


  static CustomLogger logger;


  public DumbSink() {
    logger = new CustomLogger(DumbSink.class, "unit_test");
  }

  /**
   * Statefulness for dumb things
   */
  public void open(Configuration config) throws Exception {
  }


  /**
   * Called for every message the incoming data
   */
  @Override
  public void invoke(Message msg, Context ctx) {
    System.out.println("Dumb Sink");
    logger.info(msg.toString());
    logger.info((SerializationSchema<Message>) msg1 -> msg1.toString().getBytes());
  }
}
