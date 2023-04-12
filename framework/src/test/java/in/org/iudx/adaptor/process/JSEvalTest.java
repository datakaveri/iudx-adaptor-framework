package in.org.iudx.adaptor.process;

import com.github.sisyphsu.dateparser.DateParserUtils;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Float.parseFloat;


public class JSEvalTest {


    public String stringedNum = "12.5 ppm";

    private static ScriptEngine engine;

    private static ScriptContext context;

    @BeforeAll
    public static void initialize() {
        engine = new ScriptEngineManager().getEngineByName("nashorn");
        context = new SimpleScriptContext();
        context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
    }

    @Test
    void evalSimple() throws ScriptException, NoSuchMethodException {
        String script = "function test(a) { return parseFloat(a.split(\" \")[0]) }";

        Compilable compilable = (Compilable) engine;
        final CompiledScript fn = compilable.compile(script);
        fn.eval();
        Invocable invocable = (Invocable) fn.getEngine();
        final Object[] args = new Object[1];
        args[0] = stringedNum;
        Object res = invocable.invokeFunction("test", args);
        System.out.println("Result is");
        System.out.println(parseFloat(res.toString()));

    }

    public class TestObj {
        public String name;
        public int age;
    }

    @Test
    void pojoSimple() throws ScriptException, NoSuchMethodException {
        TestObj obj = new TestObj();
        obj.name = "test";
        obj.age = 10;

        String script = "function test(a) { a.name = \"success\"; return a; }";
        Compilable compilable = (Compilable) engine;
        final CompiledScript fn = compilable.compile(script);
        fn.eval();
        Invocable invocable = (Invocable) fn.getEngine();

        Object[] args = new Object[1];
        args[0] = obj;
        final TestObj res = (TestObj) invocable.invokeFunction("test", args);
        System.out.println("Result is");
        System.out.println(res.name);
        System.out.println(res.age);
    }

    @Test
    void singleLine() throws ScriptException {
        String script = "parseFloat(a.split(\" \")[0])";

        Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);
        engineScope.put("a", stringedNum);
        Object obj = engine.eval(script, context);
        System.out.println("Result is");
        System.out.println(obj);
        System.out.println(obj.getClass().getName());

    }

    @Test
    void fixedAttributes() throws ScriptException {
        String script = "output = [parseFloat(input.split(\" \")[0])];";

        Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);
        engineScope.put("input", stringedNum);
        Object obj = engine.eval(script, context);

        System.out.println("Result is");
        System.out.println(obj);
        System.out.println(obj.getClass().getName());

    }

    @Test
    void getTime() throws ScriptException {
        String script = "var d = new Date(); datestring = d.getDate()  + '-' + (d.getMonth()+1) + '-' + d.getFullYear();";
        Object obj = engine.eval(script, context);
        System.out.println("Result is");
        System.out.println(obj);
        System.out.println(obj.getClass().getName());

    }

    @Test
    void jsonStringify() throws ScriptException {

        String script = "String(value)";

        Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);
        JSONArray arr = new JSONArray();
        arr.add(72.855479);
        arr.add(21.179428);

        engineScope.put("value", arr);
        Object obj = engine.eval(script, context);
        System.out.println("Result is");
        System.out.println(obj);
        System.out.println(obj.getClass().getName());
    }

    @Test
    void jsonTimeToDate() throws ScriptException {

        String script = "new Date(new Date(new Date('2023-04-09T23:32:27.000Z').toLocaleString('en-US', {timeZone: 'Asia/Kolkata'})).toDateString() + ' ' + value + ' UTC').toISOString().replace('.000Z', '+05:30')";
//        String script = "new Date('2023-04-09T23:32:27.000Z').toLocaleString('en-US', {timeZone: 'Asia/Kolkata'})";

        Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);
        String data = "15:27:20";

        engineScope.put("value", data);
        Object obj = engine.eval(script, context);
        System.out.println("Result is");
        System.out.println(obj);
        System.out.println(obj.getClass().getName());

        LocalDateTime localDateTime = DateParserUtils.parseDateTime(obj.toString());
        System.out.println(Timestamp.valueOf(localDateTime));
    }
}
