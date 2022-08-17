package in.org.iudx.adaptor.sql;

import in.org.iudx.adaptor.datatypes.Message;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SqlTest {

    @Test
    void connectionTest() throws InterruptedException, ClassNotFoundException, SQLException {
        Class.forName("org.apache.calcite.jdbc.Driver");

        Properties info = new Properties();
        info.setProperty("lex", "JAVA");
        Connection connection = DriverManager.getConnection("jdbc:calcite:", info);

        CalciteConnection calciteConnection= connection.unwrap(CalciteConnection.class);

        SchemaPlus rootSchema = calciteConnection.getRootSchema();

        CustomSchema schema = new CustomSchema();

//        List<Message> list = new ArrayList<>();
//
//        schema.state = list;
        rootSchema.add("listState", schema);

        Statement statement = calciteConnection.createStatement();
        String sql = "select * from listState.employees";
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            String key = rs.getString("timestamp");
            System.out.println(key);
        }
    }
}
