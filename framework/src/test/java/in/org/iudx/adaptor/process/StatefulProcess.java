package in.org.iudx.adaptor.process;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.Parser;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Deduplicator;
import in.org.iudx.adaptor.codegen.ApiConfig;

public class StatefulProcess 
  extends KeyedProcessFunction<String,Message,String> {

  /* Something temporary for now */
  private String STATE_NAME = "api state";

  private ValueState<Message> streamState;

  private static final Logger LOGGER = LogManager.getLogger(StatefulProcess.class);

  public StatefulProcess() {
  }

  @Override
  public void open(Configuration config) {
    ValueStateDescriptor<Message> stateDescriptor =
      new ValueStateDescriptor<>(STATE_NAME, Message.class);
    streamState = getRuntimeContext().getState(stateDescriptor);

    LOGGER.debug(this.STATE_NAME);

  }

  @Override
  public void processElement(Message msg,
                              Context context, Collector<String> out) throws Exception {
    Message previousMessage = streamState.value();
    /* Update state with current message if not done */
    if (previousMessage == null) {
      streamState.update(msg);
    } else {
      out.collect(msg.body);
      streamState.update(msg);
    }
  }

}
