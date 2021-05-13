package in.org.iudx.adaptor.server.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.sqlclient.PoolOptions;
import static in.org.iudx.adaptor.server.util.Constants.DATABASE_SERVICE_ADDRESS;

/**
 *The Database Verticle 
 *
 * <p>
 * The Database Verticle implementation in the IUDX Adaptor Framework exposes the
 * {@link in.org.iudx.adaptor.server.database.DatabaseService} over the Vert.x Event Bus.
 * </p>
 *
 */
public class DatabaseVerticle extends AbstractVerticle{
  
  /* Database Properties */
  private String databaseHost;
  private int databasePort;
  private String databaseName;
  private String databaseUser;
  private String databasePassword;
  private int databasePoolSize;
  private PoolOptions poolOptions;
  private PgConnectOptions connectOptions;
  private PostgresClient postgresClient;
  private DatabaseServiceImpl databaseServiceImpl;
  private MessageConsumer<JsonObject> consumer;

  @Override
  public void start() {
    
    databaseHost = config().getString("databseHost");
    databasePort = Integer.parseInt(config().getString("databasePort"));
    databaseName = config().getString("databaseName");
    databaseUser = config().getString("databaseUser");
    databasePassword = config().getString("databasePassword");
    databasePoolSize = Integer.parseInt(config().getString("databasePoolSize"));
    
    /* Set Connection Object */
    if (connectOptions == null) {
      connectOptions = new PgConnectOptions().setPort(databasePort).setHost(databaseHost)
        .setDatabase(databaseName).setUser(databaseUser).setPassword(databasePassword);
    }
    
    /* Pool options */
    if (poolOptions == null) {
      poolOptions = new PoolOptions().setMaxSize(databasePoolSize);
    }

    /* Initializing services and client*/
    postgresClient = new PostgresClient(vertx, connectOptions, poolOptions);
    databaseServiceImpl = new DatabaseServiceImpl(postgresClient);

    consumer = new ServiceBinder(vertx).setAddress(DATABASE_SERVICE_ADDRESS)
        .register(DatabaseService.class, databaseServiceImpl);
  }
  
  @Override
  public void stop() {
    new ServiceBinder(vertx).unregister(consumer);
  }
}
