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

import in.org.iudx.adaptor.sql.JsonEnumerator.JsonDataConverter;




public class JsonEnumeratorTest {

  static ObjectMapper mapper;
  static List<LinkedHashMap> objlst;

  @BeforeAll
  static void initialize() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    LinkedHashMap r1 = mapper.readValue("{\"name\":\"test\", \"index\":1}", LinkedHashMap.class);
    LinkedHashMap r2 = mapper.readValue("{\"name\":\"test2\", \"index\":2}", LinkedHashMap.class);
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
    enumerator.moveNext();
    Object[] obj2 = enumerator.current();
    System.out.println(obj2[0].getClass());
    System.out.println(obj2[0]);
  }

}



