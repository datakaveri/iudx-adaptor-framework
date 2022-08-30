package in.org.iudx.adaptor.sink;

import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.serialization.SerializationSchema;

/** DumbSink
 *  A dumb sink which prints the DataStream
 **/
public class DumbWatermarkSink implements SinkFunction<Message> {


  static CustomLogger logger;


  public DumbWatermarkSink() {
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
    logger.info((SerializationSchema<Message>) msg1 -> msg1.toString().getBytes());
    logger.debug("Timestamp = " + ctx.timestamp()
                  + "\t currentProcessingTime = " + ctx.currentProcessingTime()
                  + "\t currentWatermark = " + ctx.currentWatermark());
  }
}
