package in.org.iudx.adaptor.mockserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.properties.PropertyFileAuthentication;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BasicAuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.core.http.HttpServerResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Mock server base
 * TODO: 
 *  - This is only a template server. 
 *  - Implement functionalities in classes. For e.g Simple.java
 *  - Use those methods as callbacks here
 */
public class Server extends AbstractVerticle {

  private HttpServer server;

  private Simple simple;
  private Complex complex;

  private static final Logger LOGGER = LogManager.getLogger(Server.class);

  @SuppressWarnings("unused")
  private Router router;

  /**
   * This method is used to start the Verticle and joing a cluster
   *
   * @throws Exception which is a startup exception
   */
  @Override
  public void start() throws Exception {

    router = Router.router(vertx);

    simple = new Simple();
    complex = new Complex();


    LOGGER.debug("Info: Starting server");
    HttpServerOptions serverOptions = new HttpServerOptions();
    serverOptions.setSsl(false);

    /** Instantiate this server */
    server = vertx.createHttpServer(serverOptions);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    AuthenticationProvider authProvider = PropertyFileAuthentication.create(vertx, "basic-login.properties");
    router.route("/auth/*").handler(BasicAuthHandler.create(authProvider));



    router.get("/simpleA")
      .handler(routingContext -> {
        LOGGER.debug("Info: Received request");
        simple.getSimplePacketA(routingContext);
    });

    router.get("/simpleB")
      .handler(routingContext -> {
        LOGGER.debug("Info: Received request");
        simple.getSimplePacketB(routingContext);
    });

    router.get("/complexA")
      .handler(routingContext -> {
        LOGGER.debug("Info: Received request");
        complex.getComplexA(routingContext);
    });

    router.get("/complexB")
      .handler(routingContext -> {
        LOGGER.debug("Info: Received request");
        complex.getComplexB(routingContext);
    });
    
    router.get("/auth/simpleA")
      .handler(routingContext -> {
        LOGGER.debug("Info: Received request");
        simple.getSimplePacketA(routingContext);
    });

    /**
     * Start server 
     */
    server.requestHandler(router).listen(8080);

  }
}
