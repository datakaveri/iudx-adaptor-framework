package in.org.iudx.adaptor.process;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Deduplicator;


import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;



/* NOTE: Can't apply Transformer interface here because of serialization requirement */

public class JSProcessFunction 
  extends RichFlatMapFunction<Message, Message> {

  /* Something temporary for now */
  private String STATE_NAME = "api state";


  private String script;
  private JSTransformSpec transformSpec;
  private Function transformFunction;

  private ContextFactory contextFactory;
  private org.mozilla.javascript.Context jscontext;
  private ScriptableObject scope;

  public static final OutputTag<String> errorStream 
    = new OutputTag<String>("error") {};

  private static final Logger LOGGER = LogManager.getLogger(JSProcessFunction.class);

  private static final long serialVersionUID = 49L;

  public JSProcessFunction(String transformSpec) {
    this.transformSpec = new JSTransformSpec()
                              .setScript(new JSONObject(transformSpec)
                                                .getString("script"));
  }

  @Override
  public void open(Configuration config) {

    contextFactory = ContextFactory.getGlobal();
    jscontext = contextFactory.enterContext();
    /** TODO: Make configureable */
    jscontext.setOptimizationLevel(9);
    scope = jscontext.initStandardObjects();

    try {
      this.script = this.transformSpec.getScript();
      this.transformFunction = jscontext.compileFunction(scope, 
          this.script, "script", 1, null);
    } catch (Exception e) {
      LOGGER.error("Failed to init script");
    }

  }

  @Override
  public void flatMap(Message msg, Collector<Message> out) throws Exception {
    /* Update state with current message if not done */

    Context cx = Context.getCurrentContext();
    if (cx == null) {
      LOGGER.debug("Context is null");
      cx = contextFactory.enterContext();
      cx.getWrapFactory().setJavaPrimitiveWrap(false);
    }
    try {
      final Object[] args = new Object[1];
      args[0] = msg.body;
      String resp = org.mozilla.javascript.Context.toString(
          transformFunction.call(
            cx, scope, scope, args));
      Message transformedMessage = msg.setResponseBody(resp);
      out.collect(transformedMessage);
    } catch (Exception e) {
    }
  }

}
