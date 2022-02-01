package in.org.iudx.adaptor.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.DocumentContext;
import java.util.List;

public class JsonPathTest {

  private JSONObject data;

  private String template1
    = "{ \"metaA\": \"aaa\", \"metaB\": 12, \"data\": [ { \"id\": \"a\", \"k1\": 1, \"k2\": { \"k3\": 3 } }, { \"id\": \"b\", \"k1\": 2 } ] }";


  @Test
  void testRead() throws InterruptedException {

    try {
      List<Object> k3 = JsonPath.read(template1, "$.data");
      System.out.println(k3.get(0));
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  @Test
  void testReadIntToString() throws InterruptedException {

    ReadContext ctx = JsonPath.parse(template1);
    String val = ctx.read("$.metaB").toString();
    String val2 = ctx.read("$.metaA").toString();
    System.out.println(val);
    System.out.println(val2);

  }

  @Test
  void testWrite() throws InterruptedException {


    ReadContext ctx = JsonPath.parse(template1);
    DocumentContext doc = JsonPath.parse("{}");

    try {
      Object metaA = ctx.read("$.metaA");
      System.out.println(metaA);
      System.out.println(metaA.getClass().getName());
      Object metaB = ctx.read("$.metaB");
      System.out.println(metaB);
      System.out.println(metaB.getClass().getName());
      doc.put("$.k1", "value", metaA);
      String newdoc = doc.jsonString();
      System.out.println(newdoc);
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
