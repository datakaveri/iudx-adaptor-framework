package in.org.iudx.adaptor.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static in.org.iudx.adaptor.server.Constants.*;

/**
 * The Adaptor API Server API Verticle.
 *
 * <h1>Adaptor Server API Verticle</h1>
 *
 */

public class Server extends AbstractVerticle {

  private HttpServer server;

  @SuppressWarnings("unused")
  private Router router;

  private String catAdmin;
  private String keystore;
  private String keystorePassword;
  private boolean isSsl;
  private int port;

  private static final Logger LOGGER = LogManager.getLogger(Server.class);

  @Override
  public void start() throws Exception {
    router = Router.router(vertx);

    keystore = config().getString(KEYSTORE_PATH);
    keystorePassword = config().getString(KEYSTORE_PASSWORD);
    isSsl = config().getBoolean(IS_SSL);
    port = config().getInteger(PORT);

    HttpServerOptions serverOptions = new HttpServerOptions();

    if (isSsl) {
      serverOptions.setSsl(true)
                    .setKeyStoreOptions(new JksOptions()
                                          .setPath(keystore)
                                          .setPassword(keystorePassword));
    } else {
      serverOptions.setSsl(false);
    }
    serverOptions.setCompressionSupported(true).setCompressionLevel(5);
    /** Instantiate this server */
    server = vertx.createHttpServer(serverOptions);



    /**
     *
     * API Routes and Callbacks
     *
     */

    /** 
     * Routes - Defines the routes and callbacks
     */
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());
    router.route().handler(CorsHandler.create("*").allowedHeaders(ALLOWED_HEADERS));

  }


}
