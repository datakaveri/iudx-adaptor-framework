package in.org.iudx.adaptor.sink;

import com.github.sisyphsu.dateparser.DateParserUtils;
import in.org.iudx.adaptor.codegen.PostgresConfig;
import in.org.iudx.adaptor.datatypes.Message;
import org.apache.flink.connector.jdbc.JdbcExecutionOptions;
import org.apache.flink.connector.jdbc.JdbcSink;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PostgresSink {

    public String generateSqlQuery(String tableName, String schemaString) {
        StringBuilder generateQuery = new StringBuilder("insert into " + tableName + " (");

        JSONObject schema = new JSONObject(schemaString);

        JSONArray columns = schema.names();
        for (int i = 0; i < columns.length(); i++) {
            generateQuery.append("\"")
                    .append(columns.getString(i))
                    .append("\"")
                    .append(",");
        }

        generateQuery.deleteCharAt(generateQuery.length() - 1);
        generateQuery.append(") values (");
        generateQuery.append("?,".repeat(columns.length()));
        generateQuery.deleteCharAt(generateQuery.length() - 1);
        generateQuery.append(")");

        return generateQuery.toString();
    }

    public SinkFunction<Message> getPostgresSink(PostgresConfig postgresConfig) {
        String sqlStatement = generateSqlQuery(postgresConfig.getTableName(), postgresConfig.getTableSchema());
        return JdbcSink.sink(
                sqlStatement,
                (statement, message) -> {
                    JSONObject data = new JSONObject(message.body);
                    JSONObject schema = new JSONObject(postgresConfig.getTableSchema());
                    JSONArray columns = schema.names();
                    for (int i=0; i<columns.length(); i++) {
                        String column = columns.getString(i);
                        String columnType = schema.getString(column);
                        if (!data.has(column)) {
                            statement.setNull(i +1, java.sql.Types.NULL);
                            continue;
                        }
                        switch (columnType) {
                            case "int":
                                statement.setInt(i + 1, data.getInt(column));
                                break;
                            case "float":
                                statement.setFloat(i +1, data.getFloat(column));
                                break;
                            case "string":
                                statement.setString(i+1, data.getString(column));
                                break;
                            case "timestamp":
                                LocalDateTime localDateTime = DateParserUtils.parseDateTime(data.getString(column));
                                statement.setTimestamp(i+1, Timestamp.valueOf(localDateTime));
                                break;
                        }
                    }
                },
                JdbcExecutionOptions.builder()
                        .withBatchSize(1000)
                        .withBatchIntervalMs(200)
                        .withMaxRetries(5)
                        .build(),
                postgresConfig.getBuilder()
        );
    }
}

