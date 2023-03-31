package in.org.iudx.adaptor.server.ruleTestEndpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.org.iudx.adaptor.sql.Schema;
import in.org.iudx.adaptor.utils.JsonFlatten;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

public class SqlQueryTestEndpoint {
  public String runQuery(String query, String inputData) throws SQLException,
          ClassNotFoundException {
    Properties info = new Properties();
    info.setProperty("lex", "JAVA");
    Class.forName("org.apache.calcite.jdbc.Driver");
    try (Connection connection = DriverManager.getConnection("jdbc:calcite:", info)) {
      CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
      SchemaPlus rootSchema = calciteConnection.getRootSchema();

      JSONArray jsonArray = new JSONArray(inputData);
      Schema schema = new Schema();
      List<LinkedHashMap<String, Object>> list = new ArrayList<>();

      jsonArray.forEach(data -> {
        LinkedHashMap<String, Object> obj = null;
        try {
          obj = new JsonFlatten(
                  new ObjectMapper().readTree(data.toString())).flatten();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        list.add(obj);
      });

      schema.setData(list);
      rootSchema.add("listState", schema);

      try (Statement statement = calciteConnection.createStatement()) {
        String q = query.replace("TABLE", "listState.state");
        q = q.replace("''", "'");

        ResultSet rs = statement.executeQuery(q);
        JSONArray json = new JSONArray();
        while (rs.next()) {
          int columnCount = rs.getMetaData().getColumnCount();
          JSONObject jsonObject = new JSONObject();
          for (int i = 1; i <= columnCount; i++) {
            try {
              String column = rs.getMetaData().getColumnName(i);
              jsonObject.put(column, rs.getObject(column));
            } catch (SQLException e) {
              return e.getMessage();
            }
          }
          if (jsonObject.length() > 0) {
            json.put(jsonObject);
          }
        }
        if (json.length() > 0) {
          return json.toString();
        }
      }
    }
    return "Calcite connection failed";
  }
}
