package in.org.iudx.adaptor.process;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Scriptable;


public class JSEvalTest {
  

  public String stringedNum = "12.5 ppm";

  private static ContextFactory contextFactory;
  private static Context context;
  private static ScriptableObject scope;


  @BeforeAll
  public static void initialize() {
    contextFactory = ContextFactory.getGlobal();
    context = contextFactory.enterContext();
    context.setOptimizationLevel(9);
    scope = context.initStandardObjects();
  }

  @Test
  void evalSimple() throws InterruptedException {

    String  script = "function test(a) { return parseFloat(a.split(\" \")[0]) }";

    final Function fn = context.compileFunction(scope,
                                    script, "test", 1, null);
    final Object[] args = new Object[1];
    args[0] = stringedNum;
    Object res = fn.call(context, scope, scope, args);
    System.out.println("Result is");
    System.out.println(Context.toNumber(res));

  }

  public class TestObj {
    public String name;
    public int age;
  }

  @Test
  void pojoSimple() throws InterruptedException {

    TestObj obj = new TestObj();
    obj.name = "test";
    obj.age = 10;

    String  script = "function test(a) { a.name = \"success\"; return a; }";
    final Function fn = context.compileFunction(scope,
                                    script, "test", 1, null);
    Object[] args = new Object[1];
    args[0] = obj;
    final TestObj res = (TestObj) fn.call(context, scope, scope, args);
    System.out.println("Result is");
    System.out.println(res.name);
    System.out.println(res.age);
  }

  @Test
  void singleLine() throws InterruptedException {

    String  script = "parseFloat(a.split(\" \")[0])";

    ScriptableObject.putProperty(scope, "a", stringedNum);
    Object obj = context.evaluateString(scope,
                                    script, "test", 1, null);
    System.out.println("Result is");
    System.out.println(obj);
    System.out.println(obj.getClass().getName());

  }

  @Test
  void fixedAttributes() throws InterruptedException {

    String  script = "output = [parseFloat(input.split(\" \")[0])];";

    ScriptableObject.putProperty(scope, "input", stringedNum);
    Object obj = context.evaluateString(scope,
                                    script, "test", 1, null);
    System.out.println("Result is");
    System.out.println(obj);
    System.out.println(obj.getClass().getName());

  }

  @Test
  void getTime() throws InterruptedException {

    String  script = "var d = new Date(); datestring = d.getDate()  + '-' + (d.getMonth()+1) + '-' + d.getFullYear();";
    ScriptableObject.putProperty(scope, "input", stringedNum);
    Object obj = context.evaluateString(scope,
                                    script, "test", 1, null);
    System.out.println("Result is");
    System.out.println(obj);
    System.out.println(obj.getClass().getName());

  }
}

