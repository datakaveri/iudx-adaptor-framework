package in.org.iudx.adaptor.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.json.JSONObject;
import com.jayway.jsonpath.JsonPath;
import java.util.List;

public class JsonPathTest {

  private JSONObject data;

  private String template1
    = "{ \"metaA\": \"aaa\", \"data\": [ { \"id\": \"a\", \"k1\": 1, \"k2\": { \"k3\": 3 } }, { \"id\": \"b\", \"k1\": 2 } ] }";


  @Test
  void testA() throws InterruptedException {

    try {
      List<Object> k3 = JsonPath.read(template1, "$.data");
      System.out.println(k3.get(0));
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}
