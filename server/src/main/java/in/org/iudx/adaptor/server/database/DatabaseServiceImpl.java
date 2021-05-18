package in.org.iudx.adaptor.server.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import static in.org.iudx.adaptor.server.util.Constants.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.utils.StringUtils;

public class DatabaseServiceImpl implements DatabaseService {

  private static final Logger LOGGER = LogManager.getLogger(DatabaseServiceImpl.class);
  private PostgresClient client;

  public DatabaseServiceImpl(PostgresClient postgresClient) {
    this.client = postgresClient;
  }

  @Override
  public DatabaseService handleGenQuery(String query, Handler<AsyncResult<JsonObject>> handler) {

    JsonArray response = new JsonArray();

    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        RowSet<Row> result = pgHandler.result();
        for (Row row : result) {
          response.add(row.toJson());
        }
      }
      handler.handle(Future.succeededFuture(new JsonObject().put("result", response)));
    });
    return this;
  }

  @Override
  public DatabaseService registerUser(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService authenticateUser(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    
    JsonArray response = new JsonArray();
    String query = AUTHENTICATE_USER
                      .replace("$1", request.getString(USERNAME))
                      .replace("$2",request.getString(PASSWORD));
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        LOGGER.debug("Info: Database query succeeded");
        RowSet<Row> result = pgHandler.result();
        for (Row row : result) {
          response.add(row.toJson());
        }

        JsonObject queryRes = response.getJsonObject(0);
        if (queryRes.containsKey(EXISTS) && queryRes.getBoolean(EXISTS) == true) {
          handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
        } else {
          handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, FAILED)));
        }
      } else {
        LOGGER.error("Info: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService createAdaptor(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    
    String username = request.getString(USERNAME);
    String adaptorId = request.getString(ADAPTOR_ID);
    String data = request.getString(DATA).replace("'", "\\\"");
    
    String query = CREATE_ADAPTOR
                      .replace("$1", adaptorId)
                      .replace("$3", username)
                      .replace("$4", COMPILING)
                      .replace("$2",data);
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        LOGGER.debug("Info: Database query succeeded");
        handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));

      } else {
        LOGGER.error("Info: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService updateComplex(String query,
      Handler<AsyncResult<JsonObject>> handler) {
    
    LOGGER.debug("Info: Handling complex queries");
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        LOGGER.debug("Info: Database query succeeded");
        handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
      } else {
        LOGGER.error("Info: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    return this;
  }
}
