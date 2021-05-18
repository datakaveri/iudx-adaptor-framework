package in.org.iudx.adaptor.server.database;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface DatabaseService {
  
  @Fluent
  DatabaseService registerUser(JsonObject request, Handler<AsyncResult<JsonObject>> handler);
  
  /**
   * The authenticateUser authenticates the user with the PostgreSQL database.
   * @param request
   * @param handler
   * @return jsonObject
   */
  @Fluent
  DatabaseService authenticateUser(JsonObject request, Handler<AsyncResult<JsonObject>> handler);
  
  /**
   * The createAdaptor creates the adaptor related details in PostgreSQL database.
   * @param request
   * @param handler
   * @return jsonObject
   */
  @Fluent
  DatabaseService createAdaptor(JsonObject request, Handler<AsyncResult<JsonObject>> handler);

  /**
   * Updates the status of the Adaptor process in PostgreSQL database.
   * @param request
   * @param handler
   * @return
   */
  @Fluent
  DatabaseService updateComplex(String query, Handler<AsyncResult<JsonObject>> handler);
  
  @Fluent
  DatabaseService handleGenQuery(String query, Handler<AsyncResult<JsonObject>> handler);
  
  @GenIgnore
  static DatabaseService createProxy(Vertx vertx, String address) {
    return new DatabaseServiceVertxEBProxy(vertx, address);
  }
}
