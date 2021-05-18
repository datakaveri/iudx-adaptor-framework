package in.org.iudx.adaptor.server.database;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;

public class PostgresClient {
  private static final Logger LOGGER = LogManager.getLogger(PostgresClient.class);

  private PgPool pgPool;

  public PostgresClient(Vertx vertx, PgConnectOptions pgConnectOptions,
      PoolOptions connectionPoolOptions) {
    this.pgPool = PgPool.pool(vertx, pgConnectOptions, connectionPoolOptions);  
  }

  public Future<RowSet<Row>> executeAsync(String preparedQuerySQL) {
    //LOGGER.debug("Info : PostgreSqlClient started");
    //LOGGER.debug("Info : Query is : " + preparedQuerySQL);
    Promise<RowSet<Row>> promise = Promise.promise();
    pgPool.getConnection(connectionHandler -> {
      if (connectionHandler.succeeded()) {
        SqlConnection pgConnection = connectionHandler.result();
        pgConnection.query(preparedQuerySQL).execute(handler -> {
          if (handler.succeeded()) {
            pgConnection.close();
            promise.complete(handler.result());
          } else {
            pgConnection.close();
            LOGGER.fatal("Error: PgClient; " + handler.cause().getMessage());
            promise.fail(handler.cause().getMessage());
          }          
        });
      }
    });
    return promise.future();
  }
}
