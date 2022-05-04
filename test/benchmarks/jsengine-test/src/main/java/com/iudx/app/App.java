package com.iudx.app;

import org.mozilla.javascript.*;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

public class App {
    final static String PRIME_SOURCE = ""
            + "var N = 100000;\n"
            + "var primes = [2];\n"
            + "function is_prime(x) {\n"
            + "for (var i = 0; primes[i] * primes[i] <= x; ++i) {\n"
            + "if (x % primes[i] == 0) {\n"
            + "return false;\n"
            + "}\n"
            + "}\n"
            + "return true;\n"
            + "}\n"
            + "function main() {\n"
            + "for (var x = 3; primes.length < N; ++x) {\n"
            + "if (is_prime(x)) {\n"
            + "primes.push(x);"
            + "}\n"
            + "}\n"
            + "return primes[primes.length - 1];\n"
            + "}\n"
            + "\n";

    static void testGraalVM() throws ScriptException, NoSuchMethodException {
        long startTime = System.currentTimeMillis();
        ScriptEngine graaljsEngine = new ScriptEngineManager().getEngineByName("graal.js");
        if (graaljsEngine == null) {
            System.out.println("Graal.js not found");
        }
        graaljsEngine.eval(PRIME_SOURCE);
        Invocable inv = (Invocable) graaljsEngine;

        Object val = inv.invokeFunction("main");
        long stopTime = System.currentTimeMillis();
        System.out.println("GraalVM  ::  Time took to run == " + (stopTime - startTime));
        System.out.println(val);
    }

    static void testNashorn() throws ScriptException, NoSuchMethodException {
        long startTime = System.currentTimeMillis();
        ScriptEngine nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
        if (nashornEngine == null) {
            System.out.println("Nashorn engine not found");
        }
        nashornEngine.eval(PRIME_SOURCE);
        Invocable inv = (Invocable) nashornEngine;

        Object val = inv.invokeFunction("main");
        long stopTime = System.currentTimeMillis();
        System.out.println("Nashorn :: Time took to run == " + (stopTime - startTime));
        System.out.println(val);
    }

    static void testRhino() {
        long startTime = System.currentTimeMillis();
        ContextFactory contextFactory = ContextFactory.getGlobal();
        Context jscontext = contextFactory.enterContext();

        Map result = new HashMap<>();
        jscontext.setOptimizationLevel(9);
        ScriptableObject scope = jscontext.initStandardObjects();
        Scriptable currentScope = jscontext.newObject(scope);

        currentScope.put("result", currentScope, result);
        Script script = jscontext.compileString(PRIME_SOURCE + "result.put('result', main().toString());", "prime", 1, null);
        script.exec(jscontext, currentScope);
        long stopTime = System.currentTimeMillis();
        System.out.println("Rhino :: Time took to run == " + (stopTime - startTime));
        System.out.println(result.get("result"));
    }

    static ArrayList<Integer> primes = new ArrayList<Integer>();
    static boolean is_prime(int num) {
        for (int i = 0; primes.get(i) * primes.get(i) <= num; ++i) {
            if (num % primes.get(i) == 0) {
                return false;
            }
        }
           return true;
    }

    static long generatePrime() {
        long startTime = System.currentTimeMillis();
        int N = 100000;

        primes.add(2);

        for (int x = 3; primes.size() < N; ++x) {
            if (is_prime(x)) {
                primes.add(x);
            }
        }
        long stopTime = System.currentTimeMillis();
        return (stopTime - startTime);
    }
    static void testinJava() {
        int ITERATIONS = 100;
        List<Long> resultTime = new ArrayList<Long>();
        for(int i=0; i < ITERATIONS; i++) {
            float resTime = generatePrime();
            resultTime.add((long) resTime);
            primes = new ArrayList<Integer>();
        }

        Double res = resultTime.stream().mapToDouble(a -> a).average().getAsDouble();
        System.out.println("Plain JAVA :: Time took to run == " + res);
    }

    static void loadCustomScriptNashorn() throws ScriptException, NoSuchMethodException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        String url = "https://unpkg.com/dayjs@1.8.21/dayjs.min.js";
        engine.eval("load('"+url+"')");

        engine.eval( "function main() {\n"+
                "var d = new Date();\n" +
                "var res = dayjs(d).format('YYYY-MM-DDTHH:mm:ssZ[Z]');\n" +
                "return res" +
                "}\n");

        Invocable invocable = (Invocable) engine;

        Object result = invocable.invokeFunction("main");

        System.out.println(result);
    }

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        testRhino();
        testNashorn();
        testGraalVM();
        testinJava();
        loadCustomScriptNashorn();
    }
}
