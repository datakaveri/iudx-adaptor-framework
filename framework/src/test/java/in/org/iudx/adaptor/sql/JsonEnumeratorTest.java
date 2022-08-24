package in.org.iudx.adaptor.sql;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.util.Source;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;

import in.org.iudx.adaptor.utils.JsonFlatten;
import in.org.iudx.adaptor.sql.JsonEnumerator.JsonDataConverter;




public class JsonEnumeratorTest {

  static ObjectMapper mapper;
  static List<LinkedHashMap> objlst;

  @BeforeAll
  static void initialize() throws Exception {
    ObjectMapper mapper = new ObjectMapper();


    LinkedHashMap r1 = (LinkedHashMap) new JsonFlatten(mapper.readTree("{\"name\": {\"value\": \"test\"}, \"index\":[1,2]}")).flatten();
    LinkedHashMap r2 = (LinkedHashMap)  new JsonFlatten(mapper.readTree("{\"name\": {\"value\": \"test1\"}, \"index\":[3,4]}")).flatten();
    objlst = new ArrayList<LinkedHashMap>();
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
    enumerator.moveNext();
    Object[] obj2 = enumerator.current();
    System.out.println(obj2[0].getClass());
    System.out.println(obj2[0]);
  }

}



