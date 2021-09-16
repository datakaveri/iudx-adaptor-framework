package in.org.iudx.adaptor.source;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.configuration.Configuration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Parser;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;

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


  private ContextFactory contextFactory;
  private org.mozilla.javascript.Context jscontext;
  private ScriptableObject scope;

  private static final Logger LOGGER = LogManager.getLogger(HttpSource.class);
  /**
   * {@link HttpEntity} Constructor
   * 
   * @param ApiConfig Api configuration object
   *
   * Note: 
   *   - Only set configuration here. Don't initialize {@link HttpEntity}.
   *
   * TODO: 
   *  - Parser is prone to non-serializable params
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
    try {
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
    } catch (Exception e) {
      // Do nothing
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

    // TODO: See if this breaks during runtime
    // TODO: See why getting current context doesn't work here
    contextFactory = ContextFactory.getGlobal();
    jscontext = contextFactory.enterContext();
    jscontext.setOptimizationLevel(9);
    scope = jscontext.initStandardObjects();
    
    /* TODO: Better way of figuring out batch jobs */
    if (apiConfig.pollingInterval == -1) {
      makeApi(jscontext);
      emitMessage(ctx);
    }

    else {
      while (running) {
        makeApi(jscontext);
        emitMessage(ctx);
        Thread.sleep(apiConfig.pollingInterval);
      }
    }
  }

  private void makeApi(Context cx) {
    if (apiConfig.hasScript == true) {
      for(int i=0; i<apiConfig.scripts.size(); i++) {
        HashMap<String, String> mp = apiConfig.scripts.get(i);
        if (mp.get("in").equals("url")) {

          String val = Context.toString(cx.evaluateString(scope, 
                                                mp.get("script"),
                                                "script", 1, null));
          httpEntity.setUrl(apiConfig.url.replace(mp.get("pattern"), val));
        }
        if (mp.get("in").equals("body")) {
          String val = Context.toString(cx.evaluateString(scope, 
                                                mp.get("script"),
                                                "script", 1, null));

          httpEntity.setUrl(apiConfig.body.replace(mp.get("pattern"), val));
        }
      }

    }
  }

  @Override
  public void cancel() {

  }

}
