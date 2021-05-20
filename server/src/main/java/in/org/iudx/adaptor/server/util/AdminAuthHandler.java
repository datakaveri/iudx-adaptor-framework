package in.org.iudx.adaptor.server.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import static in.org.iudx.adaptor.server.util.Constants.*;

/**
* Handles Basic authentication for Admin.
*
*/
public class AdminAuthHandler implements Handler<RoutingContext> {
  
  private static final Logger LOGGER = LogManager.getLogger(AuthHandler.class);
  static JsonObject authCred;
  
  public static AdminAuthHandler create(JsonObject auth){
    authCred = auth;
    return new AdminAuthHandler();
  }

  @Override
  public void handle(RoutingContext routingContext) {
    
    LOGGER.debug("Authenticating admin request");
    HttpServerResponse response = routingContext.response();
    MultiMap headers = routingContext.request().headers();
    
    if(headers.contains(USERNAME) && headers.contains(PASSWORD)) {
      String userName = headers.get(USERNAME);
      String password = headers.get(PASSWORD);
      
      if((userName != null && password != null) && (!userName.isEmpty() && !password.isEmpty())) {
        if(userName.equals(authCred.getValue(USERNAME)) && password.equals(authCred.getValue(PASSWORD))) {
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
