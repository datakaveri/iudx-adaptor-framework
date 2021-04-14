package in.org.iudx.adaptor.source;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.configuration.Configuration;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Parser;

import in.org.iudx.adaptor.utils.HttpEntity;


/**
 * {@link HttpSource} - The HttpSource Class
 *
 * This extends {@link RichSourceFunction} which implements stateful functionalities.
 * This generic function exchanges meesages as {@link Message} objects.
 *
 * PO - Parser Output
 *
 * Notes: 
 *  - ?This is serializable from flink examples
 *  - The constructor can only take a serializable object, {@link ApiConfig}
 *
 */
public class HttpSource<PO> extends RichSourceFunction <Message>{

  private static final long serialVersionUID = 1L;
  private volatile boolean running = true;
  private HttpEntity httpEntity;
  private ApiConfig apiConfig;
  private Parser<PO> parser;

  private static final Logger LOGGER = LogManager.getLogger(HttpSource.class);
  /**
   * {@link HttpEntity} Constructor
   * 
   * @param ApiConfig Api configuration object
   *
   * Note: 
   *   - Only set configuration here. Don't initialize {@link HttpEntity}.
   */
  public HttpSource(ApiConfig apiConfig, Parser<PO> parser) {
    this.apiConfig = apiConfig;
    this.parser = parser;
  }

  /**
   * Retrieve stateful context info
   * 
   * @param Configuration Flink managed state configuration
   *
   * Note: 
   *   - This is where {@link HttpEntity} must be initialized
   */
  @Override
  public void open(Configuration config) throws Exception {
    super.open(config);
    httpEntity = new HttpEntity(apiConfig);
  }

  public void emitMessage(SourceContext<Message> ctx) {
    String smsg = httpEntity.getSerializedMessage();
    if (smsg.isEmpty()) {
      return;
    }
    PO msg = parser.parse(smsg);
    /* Message array */
    if (msg instanceof ArrayList) {
      ArrayList<Message> message = (ArrayList<Message>) msg;
      for (int i=0; i<message.size(); i++) {
        Message m = (Message) message.get(i);
        ctx.collectWithTimestamp(m, m.getEventTime());
        ctx.emitWatermark(new Watermark(m.getEventTime()));
      }
    } 
    /* Single object */
    if (msg instanceof Message) {
      Message m = (Message) msg;
      ctx.collectWithTimestamp(m, m.getEventTime());
      ctx.emitWatermark(new Watermark(m.getEventTime()));
    }

  }

  /**
   * Forever loop with a delay
   * 
   * @param SourceContext ?Context
   *
   * TODOS:
   *  - Is thread.sleep the best way?
   *
   */
  @Override
  public void run(SourceContext<Message> ctx) throws Exception {
    /** TODO:
     *    - Configure delays
     **/

    /* TODO: Better way of figuring out batch jobs */
    if (apiConfig.pollingInterval == -1) {
      emitMessage(ctx);
    }

    else {
      while (running) {
        emitMessage(ctx);
        Thread.sleep(apiConfig.pollingInterval);
      }
    }
  }

  @Override
  public void cancel() {

  }

}
