package in.org.iudx.adaptor.source;

import in.org.iudx.adaptor.datatypes.Message;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkOutput;


public class MessageWatermarkStrategy implements WatermarkStrategy<Message> {

  @Override
  public TimestampAssigner<Message> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
    return new MessageTimestampAssigner();
  }

  @Override
  public WatermarkGenerator<Message> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
    return new MessageWatermarkGenerator();
  }

  public class MessageTimestampAssigner implements TimestampAssigner<Message> {

    public long extractTimestamp(Message element, long recordTimestamp) {
      return element.getEventTime();
    }
  }

  public class MessageWatermarkGenerator implements WatermarkGenerator<Message> {

    public void onEvent(Message event, long eventTimestamp, WatermarkOutput output) {
    }

    public void onPeriodicEmit(WatermarkOutput output) {
    }

  }

}
