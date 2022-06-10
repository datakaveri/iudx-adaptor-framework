package in.org.iudx.adaptor.utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import in.org.iudx.adaptor.datatypes.Message;
import in.org.iudx.adaptor.process.JSPathSpec;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class JSPathProcess {

    private final JSPathSpec pathSpec;
    private final ScriptEngine engine;
    private final JSONArray pathSpecArray;

    public JSPathProcess(String jsPathSpec) {
        JSONObject jsonPathSpec = new JSONObject(jsPathSpec);
        String template = jsonPathSpec.getString("template");
        String pathSpecs = jsonPathSpec.getJSONArray("jsonPathSpec").toString();
        this.pathSpec = new JSPathSpec().setPathSpec(template, pathSpecs);
        pathSpecArray = new JSONArray(pathSpec.getPathSpec());
        engine = new ScriptEngineManager().getEngineByName("nashorn");
    }

    public String process(@NotNull Message msg) throws ScriptException {
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
                        return "";
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }

        return docCtx.jsonString();
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
