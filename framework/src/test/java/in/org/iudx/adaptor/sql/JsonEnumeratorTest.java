package in.org.iudx.adaptor.sql;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeFactory;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;

import in.org.iudx.adaptor.utils.JsonFlatten;
import in.org.iudx.adaptor.sql.JsonEnumerator.JsonDataConverter;




class JsonEnumeratorTest {

  static ObjectMapper mapper;
  static List<LinkedHashMap<String, Object>> objlst;

  @BeforeAll
  static void initialize() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    LinkedHashMap<String, Object> r1 = new JsonFlatten(mapper.readTree("{\"name\": {\"value\": " +
            "\"test\"}, \"index\":[1,2], \"observationDateTime\":\"2022-10-06 06:41:37.0\"}")).flatten();
    LinkedHashMap<String, Object> r2 = new JsonFlatten(mapper.readTree("{\"name\": {\"value\": " +
            "\"test1\"}, \"index\":[3,4], \"observationDateTime\":\"2022-10-06 06:41:37.0\"}")).flatten();
    objlst = new ArrayList<LinkedHashMap<String, Object>>();
    objlst.add(r1); objlst.add(r2);
  }

  @Test
  void testDeduce() throws Exception{
    JsonEnumerator enumerator = new JsonEnumerator(objlst);

    RelDataTypeFactory tpf = new JavaTypeFactoryImpl();
    JsonDataConverter cnv = enumerator.deduceRowType(tpf, objlst);
    RelDataType rt = cnv.getRelDataType();
    System.out.println(rt.getFieldList());
  }


  @Test
  void testEnumerator() throws Exception {
    JsonEnumerator enumerator = new JsonEnumerator(objlst);
    enumerator.moveNext();
    Object[] obj = enumerator.current();
    System.out.println(obj[0].getClass());
    System.out.println(obj[0]);
    System.out.println(obj[1].getClass());
    System.out.println(obj[1]);
    System.out.println(obj[2].getClass());
    System.out.println(obj[2]);
    System.out.println(obj[3].getClass());
    System.out.println(obj[3]);
    enumerator.moveNext();
    Object[] obj2 = enumerator.current();
    System.out.println(obj2[0].getClass());
    System.out.println(obj2[0]);
  }

}



