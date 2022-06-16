package in.org.iudx.adaptor.utils;

import in.org.iudx.adaptor.datatypes.Message;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JSProcess {

    private final JSTransformSpec transformSpec;

    private CompiledScript transformFunction;

    public JSProcess(String transformSpec) {
        this.transformSpec = new JSTransformSpec().setScript(new JSONObject(transformSpec).getString("script"));
    }

    public void initializeScriptEngine() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        String script = this.transformSpec.getScript();
        Compilable compilable = (Compilable) engine;
        this.transformFunction = compilable.compile(script);
    }

    public String process(@NotNull Message msg) throws ScriptException, NoSuchMethodException {
        this.transformFunction.eval();

        Invocable invocable = (Invocable) this.transformFunction.getEngine();
        return invocable.invokeFunction("main", msg.body).toString();
    }
}
