package in.org.iudx.adaptor.process;

import in.org.iudx.adaptor.logger.CustomLogger;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.json.JSONObject;

import in.org.iudx.adaptor.datatypes.Message;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;



/* NOTE: Can't apply Transformer interface here because of serialization requirement */

public class JSProcessFunction extends RichFlatMapFunction<Message, Message> {

    /* Something temporary for now */
    private String STATE_NAME = "api state";

    private final JSTransformSpec transformSpec;

    private CompiledScript transformFunction;

    public static final OutputTag<String> errorStream = new OutputTag<String>("error") {
    };

    CustomLogger logger;

    private static final long serialVersionUID = 49L;

    public JSProcessFunction(String transformSpec) {
        this.transformSpec = new JSTransformSpec().setScript(new JSONObject(transformSpec).getString("script"));
    }

    @Override
    public void open(Configuration config) {
        ParameterTool parameters = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        String appName = parameters.getRequired("appName");
        this.logger = (CustomLogger) CustomLogger.getLogger(JSPathProcessFunction.class, appName);

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        try {
            String script = this.transformSpec.getScript();
            Compilable compilable = (Compilable) engine;
            this.transformFunction = compilable.compile(script);
        } catch (Exception e) {
            logger.error("[JSProcessFunction] Failed to init script");
        }

    }

    @Override
    public void flatMap(Message msg, Collector<Message> out) throws Exception {
        /* Update state with current message if not done */
        try {
            this.transformFunction.eval();

            Invocable invocable = (Invocable) this.transformFunction.getEngine();
            String resp = invocable.invokeFunction("main", msg.body).toString();

            msg.setResponseBody(resp);
            out.collect(msg);
        } catch (Exception e) {
            logger.error("[JSProcessFunction] Failed to execute script", e);
        }
    }

}
