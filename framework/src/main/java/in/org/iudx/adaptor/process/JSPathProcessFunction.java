package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.json.JSONObject;
import org.json.JSONArray;

import in.org.iudx.adaptor.datatypes.Message;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.DocumentContext;


import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import javax.script.Bindings;
import javax.script.ScriptException;




/* NOTE: Can't apply Transformer interface here because of serialization requirement */

public class JSPathProcessFunction extends RichFlatMapFunction<Message, Message> {

    /* Something temporary for now */
    private String STATE_NAME = "api state";

    private final JSPathSpec pathSpec;

    private ScriptEngine engine;
    private JSONArray pathSpecArray;

    public static final OutputTag<String> errorStream = new OutputTag<String>("error") {
    };

    CustomLogger logger;

    private static final long serialVersionUID = 49L;

    public JSPathProcessFunction(String pathSpec) {
        JSONObject jsonPathSpec = new JSONObject(pathSpec);
        String template = jsonPathSpec.getString("template");
        String pathSpecs = jsonPathSpec.getJSONArray("jsonPathSpec").toString();
        this.pathSpec = new JSPathSpec().setPathSpec(template, pathSpecs);
    }

    @Override
    public void open(Configuration config) {
        ParameterTool parameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        String appName = parameters.getRequired("appName");
        this.logger = (CustomLogger) CustomLogger.getLogger(JSPathProcessFunction.class, appName);

        pathSpecArray = new JSONArray(pathSpec.getPathSpec());
        engine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    @Override
    public void flatMap(Message msg, Collector<Message> out) throws Exception {
        /* Update state with current message if not done */

        logger.info("[JSPathProcessFunction] transforming data");
        // JsonPath context
        ReadContext rCtx = JsonPath.parse(msg.body);
        DocumentContext docCtx = JsonPath.parse(pathSpec.getTemplate());

        // JS context
        ScriptContext jsContext = new SimpleScriptContext();
        jsContext.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);


        for (int i = 0; i < pathSpecArray.length(); i++) {
            JSONObject obj = pathSpecArray.getJSONObject(i);
            try {
                if (obj.has("valueModifierScript")) {
                    transform(jsContext, docCtx, rCtx, obj.getString("outputKeyPath"), obj.getString("inputValuePath"), obj.getString("valueModifierScript"));
                } else {
                    transform(docCtx, rCtx, obj.getString("outputKeyPath"), obj.getString("inputValuePath"));
                }

                if (obj.has("regexFilter")) {
                    String val = docCtx.read(obj.getString("outputKeyPath"));
                    if (val.matches(obj.getString("regexFilter"))) {
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("[JSPathProcessFunction] failed to transform data", e);
            }
        }
        String output = docCtx.jsonString();
        msg.setResponseBody(output);
        out.collect(msg);
    }

    private void transform(ScriptContext cx, DocumentContext docCtx, ReadContext rCtx, String outputKeyPath, String inputValuePath, String valueModifierScript) throws ScriptException {
        Object param = rCtx.read(inputValuePath);
        Bindings engineScope = cx.getBindings(ScriptContext.ENGINE_SCOPE);
        engineScope.put("value", param);
        Object obj = engine.eval(valueModifierScript, cx);

        docCtx.set(outputKeyPath, obj);
    }

    private void transform(DocumentContext docCtx, ReadContext rCtx, String outputKeyPath, String inputValuePath) {
        Object param = rCtx.read(inputValuePath);
        docCtx.set(outputKeyPath, param);
    }

}
