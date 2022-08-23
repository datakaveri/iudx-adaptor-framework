package in.org.iudx.adaptor.sql;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.jdbc.JavaTypeFactoryImpl;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.util.Source;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.Table;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.Test;
import org.apache.calcite.jdbc.Driver;
import java.sql.*;
import java.util.Properties;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

import in.org.iudx.adaptor.sql.JsonEnumerator.JsonDataConverter;




public class JsonTableSqlTest {

  static ObjectMapper mapper;
  static List<LinkedHashMap> objlst;
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
    LinkedHashMap r1 = mapper.readValue("{\"name\":\"test\", \"index\":1}", LinkedHashMap.class);
    LinkedHashMap r2 = mapper.readValue("{\"name\":\"test2\", \"index\":2}", LinkedHashMap.class);
    objlst = new ArrayList<LinkedHashMap>();
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
    String sql = "select * from listState.testTable where name='test2'";
    ResultSet rs = statement.executeQuery(sql);

    while (rs.next()) {
        String key = rs.getString("index");
        System.out.println(key);
        }
  }

}



