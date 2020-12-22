package in.org.iudx.adaptor.source;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;

import in.org.iudx.adaptor.datatypes.GenericJsonMessage;


public class HttpSource implements SourceFunction<GenericJsonMessage>{

  private volatile boolean running = true;
  private Api api;
  private HttpEntity httpEntity;

  public HttpSource(Api api) {
    this.api = api;
    httpEntity = new HttpEntity(api);
  }

  @Override

  public void run(SourceContext<GenericJsonMessage> ctx) throws Exception {
    /** Todo:
     *    - Configure delays
     **/
    while (running) {

      GenericJsonMessage msg = httpEntity.getMessage();
      ctx.collectWithTimestamp(msg, msg.getEventTime());
      ctx.emitWatermark(new Watermark(msg.getEventTime()));

      Thread.sleep(1);
    }
  }

  @Override
  public void cancel() {

  }

}
