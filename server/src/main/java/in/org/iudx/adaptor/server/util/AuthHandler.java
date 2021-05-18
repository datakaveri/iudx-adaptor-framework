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
 * Handles Basic authentication of APIs using PostgreSQL.
 *
 */
public class AuthHandler implements Handler<RoutingContext> {
  
  private static final Logger LOGGER = LogManager.getLogger(AuthHandler.class);
  static JsonObject authCred;
  private static DatabaseService databaseService;

  public static AuthHandler create(DatabaseService dbService) {
    databaseService = dbService;
    return new AuthHandler();
  }

  /**
   * Handles the routed authentication request.
   */
  @Override
  public void handle(RoutingContext routingContext) {
    
    LOGGER.debug("Info: Authenticating request");
    HttpServerResponse response = routingContext.response();
    MultiMap headers = routingContext.request().headers();
    JsonObject requestBody = new JsonObject();
    
    /* validates the username and password in headers */
    if(headers.contains(USERNAME) && headers.contains(PASSWORD)) {
      String userName = headers.get(USERNAME);
      String password = headers.get(PASSWORD);
      
      if((userName != null && password != null) && (!userName.isEmpty() && !password.isEmpty())) {
        requestBody.put(USERNAME, userName).put(PASSWORD, password);
        
        /* dbrequest to validate the request credentials */
        databaseService.authenticateUser(requestBody, dbauthHandler -> {
          if (dbauthHandler.succeeded()) {
            JsonObject dbRes = dbauthHandler.result();
            if (dbRes.getString(STATUS).equals(SUCCESS)) {
              LOGGER.info("Info: Successfully authenticated");
              routingContext.next();
              return;
            } else if (dbRes.getString(STATUS).equals(FAILED)) {
              LOGGER.error("Error: Request unauthorized");
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
              .setStatusCode(401)
              .end(new JsonObject()
                  .put(STATUS, FAILED)
                  .put(DESC, "Authentication failed")
                  .encode());
            }
          } else if (dbauthHandler.failed()) {
            response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .setStatusCode(400)
                    .end(new JsonObject()
                        .put(STATUS, FAILED)
                        .put(DESC, "Internal server error")
                        .encode());
          }
        });
      } else {
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(new JsonObject()
                    .put(STATUS, FAILED)
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
