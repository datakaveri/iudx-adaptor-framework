package in.org.iudx.adaptor.server.databroker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import in.org.iudx.adaptor.codegen.ApiConfig;
import in.org.iudx.adaptor.utils.HttpEntity;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.core.buffer.Buffer;

import io.vertx.core.Future;

@ExtendWith(VertxExtension.class)
public class RabbitClientTest {

  private static final Logger LOGGER = LogManager.getLogger(RabbitClientTest.class);

  private static Vertx vertxObj;
  private static WebClient webClient;
  private static WebClientOptions webConfig;
  private static RabbitWebClient rabbitWebClient;
  private static RabbitClient rabbitClient;
  private static RabbitMQOptions config;


  @BeforeAll
  @DisplayName("Deploying Verticle")
  static void startVertx(Vertx vertx, VertxTestContext testContext) {

    webConfig = new WebClientOptions();
    webConfig.setKeepAlive(true);
    webConfig.setConnectTimeout(86400000);
    webConfig.setDefaultHost("localhost");
    webConfig.setDefaultPort(15672);
    webConfig.setKeepAliveTimeout(86400000);
    webConfig.setSsl(false);

    JsonObject propObj = new JsonObject();

    propObj.put("userName", "guest");
    propObj.put("password", "guest");
    propObj.put("vHost", "IUDX");


    config = new RabbitMQOptions();
    config.setUser("guest");
    config.setPassword("guest");
    config.setHost("localhost");
    config.setPort(5672);
    config.setVirtualHost("IUDX");
    config.setAutomaticRecoveryEnabled(true);
    config.setSsl(false);


    rabbitWebClient = new RabbitWebClient(vertx, webConfig, propObj);
    rabbitClient =
        new RabbitClient(vertx, config, rabbitWebClient);

    testContext.completeNow();

  }


  @Test
  public void testCreateExchange(VertxTestContext testContext) {
    JsonObject obj = new JsonObject()
                            .put("exchangeName", "testExchange");
    Future<JsonObject> fut = rabbitClient.createExchange(obj, "IUDX");
    fut.onSuccess(resp -> {
      LOGGER.debug(resp.toString());
      testContext.completeNow();
    });
  }

  @Test
  public void testCreateQueue(VertxTestContext testContext) {
    JsonObject obj = new JsonObject()
                            .put("queueName", "testQueue");
    Future<JsonObject> fut = rabbitClient.createQueue(obj, "%2F");
    fut.onSuccess(resp -> {
      LOGGER.debug(resp.toString());
      testContext.completeNow();
    });
  }

  @Test
  public void testBinding(VertxTestContext testContext) {
    JsonObject obj = new JsonObject()
                            .put("queueName", "testQueue")
                            .put("exchangeName", "testExchange")
                            .put("entities", new JsonArray().add("abc123"));
    Future<JsonObject> fut = rabbitClient.bindQueue(obj, "%2F");
    fut.onSuccess(resp -> {
      LOGGER.debug(resp.toString());
      testContext.completeNow();
    });
  }

  @Test
  public void testPublish(VertxTestContext testContext) {
    System.out.println(rabbitClient.client.isConnected());
    rabbitClient.client.start(res -> {
      System.out.println(res.toString());
      System.out.println(rabbitClient.client.isConnected());
      Future<Void> fut = rabbitClient.client.basicPublish("testExchange", "abc123", Buffer.buffer("hello"));
      fut.onComplete(res2 -> {
        System.out.println(res2.toString());
      });
      testContext.completeNow();
    });
  }
}
