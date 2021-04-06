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


public class JSEvalTest {
  

  public String stringedNum = "12.5 ppm";

  @Test
  void evalSimple() throws InterruptedException {

    String  script = "function test(a) { return parseFloat(a.split(\" \")[0]) }";

    ContextFactory contextFactory = ContextFactory.getGlobal();
    Context context = contextFactory.enterContext();
    context.setOptimizationLevel(9);
    ScriptableObject scope = context.initStandardObjects();
    final Function fn = context.compileFunction(scope,
                                    script, "aggregate", 1, null);
    final Object[] args = new Object[1];
    args[0] = stringedNum;
    final Object res = fn.call(context, scope, scope, args);
    System.out.println("Result is");
    System.out.println(Context.toNumber(res));

  }

}

