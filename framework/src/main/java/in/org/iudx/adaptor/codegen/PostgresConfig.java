package in.org.iudx.adaptor.codegen;

import org.apache.flink.connector.jdbc.JdbcConnectionOptions;

import java.io.Serializable;

public class PostgresConfig extends JdbcConnectionOptions.JdbcConnectionOptionsBuilder implements Serializable {
    private String url;

    private String username;

    private String password;

    private String tableName;

    private String tableSchema;

    public PostgresConfig() {
        super();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public JdbcConnectionOptions getBuilder() {
        return new JdbcConnectionOptions.JdbcConnectionOptionsBuilder()
                .withUrl(url)
                .withDriverName("org.postgresql.Driver")
                .withUsername(username)
                .withPassword(password)
                .build();
    }


}
