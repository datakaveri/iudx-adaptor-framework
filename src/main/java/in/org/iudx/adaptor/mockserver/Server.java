package in.org.iudx.adaptor.mockserver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.core.http.HttpServerResponse;

public class Server extends AbstractVerticle {

  private HttpServer server;

  private Simple simple;

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


    HttpServerOptions serverOptions = new HttpServerOptions();
    serverOptions.setSsl(false);

    /** Instantiate this server */
    server = vertx.createHttpServer(serverOptions);

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.get("/simpleA")
      .handler(routingContext -> {
        simple.getSimplePacketA(routingContext);
    });

    /**
     * Start server 
     */
    server.requestHandler(router).listen(8080);

  }
}
