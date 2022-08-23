package in.org.iudx.adaptor.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;


public class ObjectMapperTest {

  static List<String> lst;
  final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void flatObj() throws Exception{
    String r1 = "{\"name\":\"test\", \"index\":1}";
    Object obj = objectMapper.readValue(r1, Object.class);
    Object[] objs = ((LinkedHashMap) obj).values().toArray();
    System.out.println(objs);
    System.out.println(objs[1].getClass());
    System.out.println(objs[1]);
  }

  @Test
  void nestedObj() throws Exception{
    String r2 = "{\"name\": {\"value\": { \"value\": \"test\"}} , \"index\": [1,2]}";

    Object obj = objectMapper.readValue(r2, Object.class);
    Object[] objs = ((LinkedHashMap) obj).values().toArray();
    System.out.println(objs[0].getClass());
    System.out.println(objs[0]);
    System.out.println(objs[1].getClass());
    System.out.println(objs[1]);
  }

}


