package in.org.iudx.adaptor.process;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    private static final Logger LOGGER = LogManager.getLogger(JSProcessFunction.class);

    private static final long serialVersionUID = 49L;

    public JSProcessFunction(String transformSpec) {
        this.transformSpec = new JSTransformSpec().setScript(new JSONObject(transformSpec).getString("script"));
    }

    @Override
    public void open(Configuration config) {

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        try {
            String script = this.transformSpec.getScript();
            Compilable compilable = (Compilable) engine;
            this.transformFunction = compilable.compile(script);
        } catch (Exception e) {
            LOGGER.error("Failed to init script");
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
            LOGGER.error("Failed to execute script", e);
        }
    }

}
