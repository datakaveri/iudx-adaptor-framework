package in.org.iudx.adaptor.server.util;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import static in.org.iudx.adaptor.server.util.Constants.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.org.iudx.adaptor.server.database.DatabaseService;

/**
 * Handles Basic authentication of APIs
 *
 */
public class AuthHandler implements Handler<RoutingContext> {
  
  private static final Logger LOGGER = LogManager.getLogger(AuthHandler.class);
  static JsonObject authCred;
  private static DatabaseService databaseService;
  
  public static AuthHandler create(JsonObject auth){
    authCred = auth;
    return new AuthHandler();
  }
  
  /*
   * public static AuthHandler create(DatabaseService dbService){ databaseService = dbService;
   * return new AuthHandler(); }
   */
  
  @Override
  public void handle(RoutingContext routingContext) {
    
    LOGGER.debug("Authenticating request");
    HttpServerResponse response = routingContext.response();
    MultiMap headers = routingContext.request().headers();
    
    if(headers.contains("username") && headers.contains("password")) {
      String userName = headers.get("username");
      String password = headers.get("password");
      
      if((userName != null && password != null) && (!userName.isEmpty() && !password.isEmpty())) {
        if(userName.equals(authCred.getValue("username")) && password.equals(authCred.getValue("password"))) {
          LOGGER.info("Successfully authenticated");
          routingContext.next();
          return;
        } else {
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                  .setStatusCode(401)
                  .end(new JsonObject()
                      .put(STATUS, ERROR)
                      .put(DESC, "Authorization failed")
                      .encode());
        }
      } else {
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(new JsonObject()
                    .put(STATUS, ERROR)
                    .put(DESC, "Missing username/password header value")
                    .encode());
      }
      
    }else {
      response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
              .setStatusCode(400)
              .end(new JsonObject()
                  .put(STATUS, ERROR)
                  .put(DESC, "Missing username/password header")
                  .encode());
    }
  }

}
