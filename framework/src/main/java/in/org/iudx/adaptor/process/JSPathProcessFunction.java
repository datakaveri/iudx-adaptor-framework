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
import org.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.codegen.Transformer;
import in.org.iudx.adaptor.codegen.Deduplicator;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.DocumentContext;


import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;



/* NOTE: Can't apply Transformer interface here because of serialization requirement */

public class JSPathProcessFunction 
  extends RichFlatMapFunction<Message, Message> {

  /* Something temporary for now */
  private String STATE_NAME = "api state";

  private JSPathSpec pathSpec;
  private Function transformFunction;

  private ContextFactory contextFactory;
  private org.mozilla.javascript.Context jscontext;
  private ScriptableObject scope;
  private JSONArray pathSpecArray;

  public static final OutputTag<String> errorStream 
    = new OutputTag<String>("error") {};

  private static final Logger LOGGER = LogManager.getLogger(JSPathProcessFunction.class);

  private static final long serialVersionUID = 49L;

  public JSPathProcessFunction(String pathSpec) {
    JSONObject jsonPathSpec = new JSONObject(pathSpec);
    String template = jsonPathSpec.getString("template");
    String pathSpecArray = jsonPathSpec.getJSONArray("jsonPathSpec").toString();
    this.pathSpec = new JSPathSpec().setPathSpec(template, pathSpecArray);
  }

  @Override
  public void open(Configuration config) {

    pathSpecArray = new JSONArray(pathSpec.getPathSpec());

    contextFactory = ContextFactory.getGlobal();
    jscontext = contextFactory.enterContext();
    /** TODO: Make configureable */
    jscontext.setOptimizationLevel(9);
    scope = jscontext.initStandardObjects();
  }

  @Override
  public void flatMap(Message msg, Collector<Message> out) throws Exception {
    /* Update state with current message if not done */


    // JsonPath context
    ReadContext rCtx = JsonPath.parse(msg.body);
    DocumentContext docCtx = JsonPath.parse(pathSpec.getTemplate());

    // Rhino context
    Context cx = Context.getCurrentContext();
    scope = cx.initStandardObjects();


    if (cx == null) {
      LOGGER.debug("Context is null");
      cx = contextFactory.enterContext();
      cx.getWrapFactory().setJavaPrimitiveWrap(false);
      scope = cx.initStandardObjects();
    }


    for (int i=0; i<pathSpecArray.length(); i++) {
      JSONObject obj = pathSpecArray.getJSONObject(i);
      try {
        if (obj.has("valueModifierScript")) {
          transform(cx, docCtx, rCtx, obj.getString("outputKeyPath"),
                              obj.getString("inputValuePath"),
                              obj.getString("valueModifierScript"));
        } else {
          transform(docCtx, rCtx, obj.getString("outputKeyPath"),
                              obj.getString("inputValuePath"));
        }

        if (obj.has("regexFilter")) {
          String val = docCtx.read(obj.getString("outputKeyPath"));
          if(val.matches(obj.getString("regexFilter"))) {
            return;
          }
        }
      } catch (Exception e) {
        LOGGER.debug("Failed");
        LOGGER.debug(e);
      }
    }
    String outstr = docCtx.jsonString();
    msg.setResponseBody(outstr);
    out.collect(msg);
  }

  private void transform(Context cx, DocumentContext docCtx, ReadContext rCtx, 
                          String outputKeyPath, String inputValuePath, String valueModifierScript) {
    Object param = rCtx.read(inputValuePath);
    ScriptableObject.putProperty(scope, "value", param);
    Object obj = cx.evaluateString(scope,
        valueModifierScript,
        "script", 1, null);
    docCtx.set(outputKeyPath, obj);
  }

  private void transform(DocumentContext docCtx, ReadContext rCtx, 
                          String outputKeyPath, String inputValuePath) {
    Object param = rCtx.read(inputValuePath);
    docCtx.set(outputKeyPath, param);
  }

}
