package in.org.iudx.adaptor.sql;

import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.Properties;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

import in.org.iudx.adaptor.utils.JsonFlatten;




public class JsonTableSqlTest {

  static ObjectMapper mapper;
  static List<LinkedHashMap<String, Object>> objlst;
  static JsonArrayListTable tbl;

  final class TestSchema extends AbstractSchema {
    @Override
    protected Map<String, Table> getTableMap() {
        return Collections.singletonMap("testTable", tbl);
    }
  }

  @BeforeAll
  static void initialize() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    // Make table
    LinkedHashMap<String, Object> r1 = new JsonFlatten(mapper.readTree("{\"name\": {\"value\": \"test\"}, \"index\":[1,2]}")).flatten();
    LinkedHashMap<String, Object> r2 = new JsonFlatten(mapper.readTree("{\"name\": {\"value\": \"test1\"}, \"index\":[3,4]}")).flatten();
    objlst = new ArrayList<LinkedHashMap<String, Object>>();
    objlst.add(r1); objlst.add(r2);
    tbl = new JsonArrayListTable(objlst);

    // Make schema


  }

  @Test
  void sqlQuery() throws Exception{
    Properties info = new Properties();
    info.setProperty("lex", "JAVA");
    Connection connection = DriverManager.getConnection("jdbc:calcite:", info);
    CalciteConnection calciteConnection= connection.unwrap(CalciteConnection.class);
    SchemaPlus rootSchema = calciteConnection.getRootSchema();
    TestSchema schema = new TestSchema();
    rootSchema.add("listState", schema);
    Statement statement = calciteConnection.createStatement();
    String sql = "select * from listState.testTable where `name.value`='test1'";
    ResultSet rs = statement.executeQuery(sql);

    while (rs.next()) {
        int key = rs.getInt("index.0");
        System.out.println("Result is = ");
        System.out.println(key);
        }
  }

}



