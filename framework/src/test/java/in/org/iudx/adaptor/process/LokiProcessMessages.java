package in.org.iudx.adaptor.process;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import in.org.iudx.adaptor.datatypes.Message;
import org.json.JSONObject;

public class LokiProcessMessages extends ProcessFunction<Message, Tuple2<Message, Integer>> {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public static final OutputTag<Message> errorStream = new OutputTag<Message>("error") {};
  public static final OutputTag<Message> successStream = new OutputTag<Message>("success") {};

  @Override
  public void processElement(Message value,
      ProcessFunction<Message, Tuple2<Message, Integer>>.Context ctx,
      Collector<Tuple2<Message, Integer>> out) throws Exception {
    
    JSONObject body = new JSONObject(value.body);
    Integer flag = body.getInt("k1");
    if(flag % 2 != 0) {
      ctx.output(errorStream, value);
    } else if(flag % 2 == 0) {
      ctx.output(successStream, value);
      out.collect(new Tuple2<>(value,1));
    }
    
    
  }

}
