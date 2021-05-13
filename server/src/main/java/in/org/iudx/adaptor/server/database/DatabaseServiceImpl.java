package in.org.iudx.adaptor.server.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class DatabaseServiceImpl  implements DatabaseService{

  private PostgresClient client;
  
  public DatabaseServiceImpl(PostgresClient postgresClient) {
    this.client = postgresClient;
  }

  @Override
  public DatabaseService handleQuery(String query, Handler<AsyncResult<JsonObject>> handler) {
    
    JsonArray response = new JsonArray();
    
    client.executeAsync(query).onComplete(pgHandler ->{
      if(pgHandler.succeeded()) {
         RowSet<Row> result = pgHandler.result();
        for (Row row : result) {
          response.add(row.toJson());
        }
      }
      handler.handle(Future.succeededFuture(new JsonObject().put("result",response)));
    });
    return this;
  }

  @Override
  public DatabaseService registerUser(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DatabaseService authenticateUser(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    // TODO Auto-generated method stub
    return null;
  }
  
  

}
