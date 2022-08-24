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

import in.org.iudx.adaptor.utils.JsonFlatten;
import in.org.iudx.adaptor.sql.JsonEnumerator.JsonDataConverter;




public class JsonArrayListTableTest {

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
  void initTableTest() throws Exception{
    RelDataTypeFactory tpf = new JavaTypeFactoryImpl();
    JsonArrayListTable tbl = new JsonArrayListTable(objlst);
    RelDataType rt = tbl.getRowType(tpf);
    System.out.println(rt.getFieldNames());
  }

}



