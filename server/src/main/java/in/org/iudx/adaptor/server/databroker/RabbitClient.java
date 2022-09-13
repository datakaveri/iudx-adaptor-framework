package in.org.iudx.adaptor.server.databroker;

import static in.org.iudx.adaptor.server.databroker.Constants.ALLOW;
import static in.org.iudx.adaptor.server.databroker.Constants.ALL_NOT_FOUND;
import static in.org.iudx.adaptor.server.databroker.Constants.AUTO_DELETE;
import static in.org.iudx.adaptor.server.databroker.Constants.CHECK_CREDENTIALS;
import static in.org.iudx.adaptor.server.databroker.Constants.CONFIGURE;
import static in.org.iudx.adaptor.server.databroker.Constants.DATA_ISSUE;
import static in.org.iudx.adaptor.server.databroker.Constants.DATA_WILDCARD_ROUTINGKEY;
import static in.org.iudx.adaptor.server.databroker.Constants.DENY;
import static in.org.iudx.adaptor.server.databroker.Constants.DETAIL;
import static in.org.iudx.adaptor.server.databroker.Constants.DOWNSTREAM_ISSUE;
import static in.org.iudx.adaptor.server.databroker.Constants.DURABLE;
import static in.org.iudx.adaptor.server.databroker.Constants.ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.EXCHANGE;
import static in.org.iudx.adaptor.server.databroker.Constants.EXCHANGE_CREATE_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.EXCHANGE_DELETE_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.EXCHANGE_EXISTS;
import static in.org.iudx.adaptor.server.databroker.Constants.EXCHANGE_EXISTS_WITH_DIFFERENT_PROPERTIES;
import static in.org.iudx.adaptor.server.databroker.Constants.EXCHANGE_FOUND;
import static in.org.iudx.adaptor.server.databroker.Constants.EXCHANGE_NOT_FOUND;
import static in.org.iudx.adaptor.server.databroker.Constants.EXCHANGE_TYPE;
import static in.org.iudx.adaptor.server.databroker.Constants.FAILURE;
import static in.org.iudx.adaptor.server.databroker.Constants.HEARTBEAT;
import static in.org.iudx.adaptor.server.databroker.Constants.ID;
import static in.org.iudx.adaptor.server.databroker.Constants.INTERNAL_ERROR_CODE;
import static in.org.iudx.adaptor.server.databroker.Constants.NETWORK_ISSUE;
import static in.org.iudx.adaptor.server.databroker.Constants.NONE;
import static in.org.iudx.adaptor.server.databroker.Constants.PASSWORD;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_ADAPTOR_LOGS;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_ALREADY_EXISTS;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_ALREADY_EXISTS_WITH_DIFFERENT_PROPERTIES;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_BIND_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_CREATE_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_DATA;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_DELETE_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_DOES_NOT_EXISTS;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_EXCHANGE_NOT_FOUND;
import static in.org.iudx.adaptor.server.databroker.Constants.QUEUE_LIST_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.READ;
import static in.org.iudx.adaptor.server.databroker.Constants.REDIS_LATEST;
import static in.org.iudx.adaptor.server.databroker.Constants.REQUEST_DELETE;
import static in.org.iudx.adaptor.server.databroker.Constants.REQUEST_GET;
import static in.org.iudx.adaptor.server.databroker.Constants.REQUEST_POST;
import static in.org.iudx.adaptor.server.databroker.Constants.REQUEST_PUT;
import static in.org.iudx.adaptor.server.databroker.Constants.SUCCESS;
import static in.org.iudx.adaptor.server.databroker.Constants.SUCCESS_CODE;
import static in.org.iudx.adaptor.server.databroker.Constants.TAGS;
import static in.org.iudx.adaptor.server.databroker.Constants.TITLE;
import static in.org.iudx.adaptor.server.databroker.Constants.TOPIC_PERMISSION;
import static in.org.iudx.adaptor.server.databroker.Constants.TOPIC_PERMISSION_ALREADY_SET;
import static in.org.iudx.adaptor.server.databroker.Constants.TOPIC_PERMISSION_SET_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.TOPIC_PERMISSION_SET_SUCCESS;
import static in.org.iudx.adaptor.server.databroker.Constants.TYPE;
import static in.org.iudx.adaptor.server.databroker.Constants.VHOST_ALREADY_EXISTS;
import static in.org.iudx.adaptor.server.databroker.Constants.VHOST_CREATE_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.VHOST_DELETE_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.VHOST_LIST_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.VHOST_NOT_FOUND;
import static in.org.iudx.adaptor.server.databroker.Constants.VHOST_PERMISSIONS;
import static in.org.iudx.adaptor.server.databroker.Constants.VHOST_PERMISSIONS_WRITE;
import static in.org.iudx.adaptor.server.databroker.Constants.VHOST_PERMISSION_SET_ERROR;
import static in.org.iudx.adaptor.server.databroker.Constants.WRITE;
import static in.org.iudx.adaptor.server.databroker.Util.encodeValue;
import static in.org.iudx.adaptor.server.databroker.Util.getResponseJson;
import static in.org.iudx.adaptor.server.databroker.Util.isGroupId;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;

public class RabbitClient {

  private static final Logger LOGGER = LogManager.getLogger(RabbitClient.class);

  public RabbitMQClient client;
  private RabbitWebClient webClient;
  private String amqpUrl;
  private int amqpPort;


  public RabbitClient(Vertx vertx, RabbitMQOptions rabbitConfigs,
                      RabbitWebClient webClient) {

    this.client = getRabbitMQClient(vertx, rabbitConfigs);
    this.webClient = webClient;
    client.start(clientStartupHandler -> {
      if (clientStartupHandler.succeeded()) {
        LOGGER.info("Info : rabbit MQ client started");
      } else if (clientStartupHandler.failed()) {
        LOGGER.fatal("Fail : rabbit MQ client startup failed.");
      }
    });
  }

  private RabbitMQClient getRabbitMQClient(Vertx vertx, RabbitMQOptions rabbitConfigs) {
    return RabbitMQClient.create(vertx, rabbitConfigs);
  }

  /**
   * The createExchange implements the create exchange.
   *
   * @param request which is a Json object
   * @Param vHost virtual-host
   * @return response which is a Future object of promise of Json type
   **/
  public Future<JsonObject> createExchange(JsonObject request, String vHost) {
    LOGGER.trace("Info : RabbitClient#createExchage() started");
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String exchangeName = request.getString("exchangeName");
      String url = "/api/exchanges/" + vHost + "/" + encodeValue(exchangeName);
      JsonObject obj = new JsonObject();
      obj.put(TYPE, EXCHANGE_TYPE);
      obj.put(AUTO_DELETE, false);
      obj.put(DURABLE, true);
      webClient.requestAsync(REQUEST_PUT, url, obj).onComplete(requestHandler -> {
        if (requestHandler.succeeded()) {
          JsonObject responseJson = new JsonObject();
          HttpResponse<Buffer> response = requestHandler.result();
          int statusCode = response.statusCode();
          if (statusCode == HttpStatus.SC_CREATED) {
            responseJson.put(EXCHANGE, exchangeName);
          } else if (statusCode == HttpStatus.SC_NO_CONTENT) {
            responseJson = getResponseJson(HttpStatus.SC_CONFLICT, FAILURE, EXCHANGE_EXISTS);
          } else if (statusCode == HttpStatus.SC_BAD_REQUEST) {
            responseJson = getResponseJson(statusCode, FAILURE,
                    EXCHANGE_EXISTS_WITH_DIFFERENT_PROPERTIES);
          }
          LOGGER.debug("Success : " + responseJson);
          promise.complete(responseJson);
        } else {
          JsonObject errorJson = getResponseJson(HttpStatus.SC_INTERNAL_SERVER_ERROR, ERROR,
                  EXCHANGE_CREATE_ERROR);
          LOGGER.error("Fail : " + requestHandler.cause());
          promise.fail(errorJson.toString());
        }
      });
    }
    return promise.future();
  }

  Future<JsonObject> getExchangeDetails(JsonObject request, String vHost) {
    LOGGER.trace("Info : RabbitClient#getExchange() started");
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String exchangeName = request.getString("exchangeName");
      String url = "/api/exchanges/" + vHost + "/" + encodeValue(exchangeName);
      webClient.requestAsync(REQUEST_GET, url).onComplete(requestHandler -> {
        if (requestHandler.succeeded()) {
          JsonObject responseJson = new JsonObject();
          HttpResponse<Buffer> response = requestHandler.result();
          int statusCode = response.statusCode();
          if (statusCode == HttpStatus.SC_OK) {
            responseJson = new JsonObject(response.body().toString());
            LOGGER.debug("Success : " + responseJson);
            promise.complete(responseJson);
          } else {
            responseJson = getResponseJson(statusCode, FAILURE, EXCHANGE_NOT_FOUND);
            promise.fail(responseJson.toString());
          }
        } else {
          JsonObject errorJson =
                  getResponseJson(HttpStatus.SC_INTERNAL_SERVER_ERROR, ERROR, EXCHANGE_NOT_FOUND);
          LOGGER.error("Error : " + requestHandler.cause());
          promise.fail(errorJson.toString());
        }
      });
    }
    return promise.future();
  }

  Future<JsonObject> getExchange(JsonObject request, String vhost) {
    JsonObject response = new JsonObject();
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String exchangeName = request.getString("id");
      String url;
      url = "/api/exchanges/" + vhost + "/" + encodeValue(exchangeName);
      webClient.requestAsync(REQUEST_GET, url).onComplete(result -> {
        if (result.succeeded()) {
          int status = result.result().statusCode();
          response.put(TYPE, status);
          if (status == HttpStatus.SC_OK) {
            response.put(TITLE, SUCCESS);
            response.put(DETAIL, EXCHANGE_FOUND);
          } else if (status == HttpStatus.SC_NOT_FOUND) {
            response.put(TITLE, FAILURE);
            response.put(DETAIL, EXCHANGE_NOT_FOUND);
          } else {
            response.put("getExchange_status", status);
            promise.fail("getExchange_status" + result.cause());
          }
        } else {
          response.put("getExchange_error", result.cause());
          promise.fail("getExchange_error" + result.cause());
        }
        LOGGER.debug("getExchange method response : " + response);
        promise.tryComplete(response);
      });

    } else {
      promise.fail("exchangeName not provided");
    }
    return promise.future();

  }

  /**
   * The deleteExchange implements the delete exchange operation.
   *
   * @param request which is a Json object
   * @Param VHost virtual-host
   * @return response which is a Future object of promise of Json type
   */
  Future<JsonObject> deleteExchange(JsonObject request, String vHost) {
    LOGGER.trace("Info : RabbitClient#deleteExchange() started");
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String exchangeName = request.getString("exchangeName");
      String url = "/api/exchanges/" + vHost + "/" + encodeValue(exchangeName);
      webClient.requestAsync(REQUEST_DELETE, url).onComplete(requestHandler -> {
        if (requestHandler.succeeded()) {
          JsonObject responseJson = new JsonObject();
          HttpResponse<Buffer> response = requestHandler.result();
          int statusCode = response.statusCode();
          if (statusCode == HttpStatus.SC_NO_CONTENT) {
            responseJson = new JsonObject();
            responseJson.put(EXCHANGE, exchangeName);
          } else {
            responseJson = getResponseJson(statusCode, FAILURE, EXCHANGE_NOT_FOUND);
            LOGGER.debug("Success : " + responseJson);
          }
          promise.complete(responseJson);
        } else {
          JsonObject errorJson = getResponseJson(HttpStatus.SC_INTERNAL_SERVER_ERROR, ERROR,
                  EXCHANGE_DELETE_ERROR);
          LOGGER.error("Error : " + requestHandler.cause());
          promise.fail(errorJson.toString());
        }
      });
    }
    return promise.future();
  }

  /**
   * The listExchangeSubscribers implements the list of bindings for an exchange (source).
   *
   * @param request which is a Json object
   * @param vhost virtual-host
   * @return response which is a Future object of promise of Json type
   */
  Future<JsonObject> listExchangeSubscribers(JsonObject request, String vhost) {
    LOGGER.trace("Info : RabbitClient#listExchangeSubscribers() started");
    Promise<JsonObject> promise = Promise.promise();
    JsonObject finalResponse = new JsonObject();
    if (request != null && !request.isEmpty()) {
      String exchangeName = request.getString(ID);
      String url =
              "/api/exchanges/" + vhost + "/" + encodeValue(exchangeName) + "/bindings/source";
      webClient.requestAsync(REQUEST_GET, url).onComplete(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          if (response != null && !response.equals(" ")) {
            int status = response.statusCode();
            if (status == HttpStatus.SC_OK) {
              Buffer body = response.body();
              if (body != null) {
                JsonArray jsonBody = new JsonArray(body.toString());
                Map res = jsonBody.stream().map(JsonObject.class::cast)
                        .collect(Collectors.toMap(json -> json.getString("destination"),
                                json -> new JsonArray().add(json.getString("routing_key")),
                                Util.bindingMergeOperator));
                LOGGER.debug("Info : exchange subscribers : " + jsonBody);
                finalResponse.clear().mergeIn(new JsonObject(res));
                LOGGER.debug("Info : final Response : " + finalResponse);
                if (finalResponse.isEmpty()) {
                  finalResponse.clear().mergeIn(
                          getResponseJson(HttpStatus.SC_NOT_FOUND, FAILURE, EXCHANGE_NOT_FOUND),
                          true);
                }
              }
            } else if (status == HttpStatus.SC_NOT_FOUND) {
              finalResponse.mergeIn(
                      getResponseJson(HttpStatus.SC_NOT_FOUND, FAILURE, EXCHANGE_NOT_FOUND), true);
            }
          }
          promise.complete(finalResponse);
          LOGGER.debug("Success :" + finalResponse);
        } else {
          LOGGER.error("Fail : Listing of Exchange failed - ", ar.cause());
          JsonObject error = getResponseJson(500, FAILURE, "Internal server error");
          promise.fail(error.toString());
        }
      });
    }
    return promise.future();
  }

  /**
   * The createQueue implements the create queue operation.
   *
   * @param request which is a Json object
   * @param vhost virtual-host
   * @return response which is a Future object of promise of Json type
   */
  public Future<JsonObject> createQueue(JsonObject request, String vhost) {
    LOGGER.trace("Info : RabbitClient#createQueue() started");
    Promise<JsonObject> promise = Promise.promise();
    JsonObject finalResponse = new JsonObject();
    if (request != null && !request.isEmpty()) {
      String queueName = request.getString("queueName");
      String url = "/api/queues/" + vhost + "/" + encodeValue(queueName);// "durable":true
      JsonObject configProp = new JsonObject();
      JsonObject arguments = new JsonObject();
      arguments.put(Constants.X_MESSAGE_TTL_NAME, Constants.X_MESSAGE_TTL_VALUE)
              .put(Constants.X_MAXLENGTH_NAME, Constants.X_MAXLENGTH_VALUE)
              .put(Constants.X_QUEUE_MODE_NAME, Constants.X_QUEUE_MODE_VALUE);
      configProp.put(Constants.X_QUEUE_TYPE, true);
      configProp.put(Constants.X_QUEUE_ARGUMENTS, arguments);
      webClient.requestAsync(REQUEST_PUT, url, configProp).onComplete(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          if (response != null && !response.equals(" ")) {
            int status = response.statusCode();
            if (status == HttpStatus.SC_CREATED) {
              finalResponse.put(Constants.QUEUE, queueName);
            } else if (status == HttpStatus.SC_NO_CONTENT) {
              finalResponse.mergeIn(
                      getResponseJson(HttpStatus.SC_CONFLICT, FAILURE, QUEUE_ALREADY_EXISTS),
                      true);
            } else if (status == HttpStatus.SC_BAD_REQUEST) {
              finalResponse.mergeIn(getResponseJson(status, FAILURE,
                      QUEUE_ALREADY_EXISTS_WITH_DIFFERENT_PROPERTIES), true);
            }
          }
          promise.complete(finalResponse);
          LOGGER.info("Success : " + finalResponse);
        } else {
          LOGGER.error("Fail : Creation of Queue failed - ", ar.cause());
          finalResponse.mergeIn(getResponseJson(500, FAILURE, QUEUE_CREATE_ERROR));
          promise.fail(finalResponse.toString());
        }
      });
    }
    return promise.future();
  }


  /**
   * The deleteQueue implements the delete queue operation.
   *
   * @param request which is a Json object
   * @param vhost virtual-host
   * @return response which is a Future object of promise of Json type
   */
  public Future<JsonObject> deleteQueue(JsonObject request, String vhost) {
    LOGGER.trace("Info : RabbitClient#deleteQueue() started");
    Promise<JsonObject> promise = Promise.promise();
    JsonObject finalResponse = new JsonObject();
    if (request != null && !request.isEmpty()) {
      String queueName = request.getString("queueName");
      LOGGER.debug("Info : queuName" + queueName);
      String url = "/api/queues/" + vhost + "/" + encodeValue(queueName);
      webClient.requestAsync(REQUEST_DELETE, url).onComplete(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          if (response != null && !response.equals(" ")) {
            int status = response.statusCode();
            if (status == HttpStatus.SC_NO_CONTENT) {
              finalResponse.put(Constants.QUEUE, queueName);
            } else if (status == HttpStatus.SC_NOT_FOUND) {
              finalResponse.mergeIn(getResponseJson(status, FAILURE, QUEUE_DOES_NOT_EXISTS));
            }
          }
          LOGGER.info(finalResponse);
          promise.complete(finalResponse);
        } else {
          LOGGER.error("Fail : deletion of queue failed - ", ar.cause());
          finalResponse.mergeIn(getResponseJson(500, FAILURE, QUEUE_DELETE_ERROR));
          promise.fail(finalResponse.toString());
        }
      });
    }
    return promise.future();
  }

  /**
   * The bindQueue implements the bind queue to exchange by routing key.
   *
   * @param request which is a Json object
   * @param vhost virtual-host
   * @return response which is a Future object of promise of Json type
   */
  public Future<JsonObject> bindQueue(JsonObject request, String vhost) {
    LOGGER.trace("Info : RabbitClient#bindQueue() started");
    JsonObject finalResponse = new JsonObject();
    JsonObject requestBody = new JsonObject();
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String exchangeName = request.getString("exchangeName");
      String queueName = request.getString("queueName");
      JsonArray entities = request.getJsonArray("entities");
      int arrayPos = entities.size() - 1;
      String url = "/api/bindings/" + vhost + "/e/" + encodeValue(exchangeName) + "/q/"
              + encodeValue(queueName);
      for (Object rkey : entities) {
        requestBody.put("routing_key", rkey.toString());
        webClient.requestAsync(REQUEST_POST, url, requestBody).onComplete(ar -> {
          if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            if (response != null && !response.equals(" ")) {
              int status = response.statusCode();
              LOGGER.info("Info : Binding " + rkey.toString() + "Success. Status is " + status);
              if (status == HttpStatus.SC_CREATED) {
                finalResponse.put(Constants.EXCHANGE, exchangeName);
                finalResponse.put(Constants.QUEUE, queueName);
                finalResponse.put(Constants.ENTITIES, entities);
              } else if (status == HttpStatus.SC_NOT_FOUND) {
                finalResponse
                        .mergeIn(getResponseJson(status, FAILURE, QUEUE_EXCHANGE_NOT_FOUND));
              }
            }
            if (rkey == entities.getValue(arrayPos)) {
              LOGGER.debug("Success : " + finalResponse);
              promise.complete(finalResponse);
            }
          } else {
            LOGGER.error("Fail : Binding of Queue failed - ", ar.cause());
            finalResponse.mergeIn(getResponseJson(500, FAILURE, QUEUE_BIND_ERROR));
            promise.fail(finalResponse.toString());
          }
        });
      }
    }
    return promise.future();
  }

  /**
   * The unbindQueue implements the unbind queue to exchange by routing key.
   *
   * @param request which is a Json object
   * @param vhost virtual-host
   * @return response which is a Future object of promise of Json type
   */
  Future<JsonObject> unbindQueue(JsonObject request, String vhost) {
    LOGGER.trace("Info : RabbitClient#unbindQueue() started");
    JsonObject finalResponse = new JsonObject();
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String exchangeName = request.getString("exchangeName");
      String queueName = request.getString("queueName");
      JsonArray entities = request.getJsonArray("entities");
      int arrayPos = entities.size() - 1;
      for (Object rkey : entities) {
        String url = "/api/bindings/" + vhost + "/e/" + encodeValue(exchangeName) + "/q/"
                + encodeValue(queueName) + "/" + encodeValue((String) rkey);
        webClient.requestAsync(REQUEST_DELETE, url).onComplete(ar -> {
          if (ar.succeeded()) {
            HttpResponse<Buffer> response = ar.result();
            if (response != null && !response.equals(" ")) {
              int status = response.statusCode();
              if (status == HttpStatus.SC_NO_CONTENT) {
                finalResponse.put(Constants.EXCHANGE, exchangeName);
                finalResponse.put(Constants.QUEUE, queueName);
                finalResponse.put(Constants.ENTITIES, entities);
              } else if (status == HttpStatus.SC_NOT_FOUND) {
                finalResponse.mergeIn(getResponseJson(status, FAILURE, ALL_NOT_FOUND));
              }
            }
            if (rkey == entities.getValue(arrayPos)) {
              LOGGER.debug("Success : " + finalResponse);
              promise.complete(finalResponse);
            }
          } else {
            LOGGER.error("Fail : Unbinding of Queue failed", ar.cause());
            finalResponse.mergeIn(getResponseJson(500, FAILURE, QUEUE_BIND_ERROR));
            promise.fail(finalResponse.toString());
          }
        });
      }
    }
    return promise.future();
  }

  /**
   * The createvHost implements the create virtual host operation.
   *
   * @param request which is a Json object
   * @return response which is a Future object of promise of Json type
   */
  Future<JsonObject> createvHost(JsonObject request) {
    LOGGER.trace("Info : RabbitClient#createvHost() started");
    JsonObject finalResponse = new JsonObject();
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String vhost = request.getString("vHost");
      String url = "/api/vhosts/" + encodeValue(vhost);
      webClient.requestAsync(REQUEST_PUT, url).onComplete(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          if (response != null && !response.equals(" ")) {
            int status = response.statusCode();
            if (status == HttpStatus.SC_CREATED) {
              finalResponse.put(Constants.VHOST, vhost);
            } else if (status == HttpStatus.SC_NO_CONTENT) {
              finalResponse.mergeIn(
                      getResponseJson(HttpStatus.SC_CONFLICT, FAILURE, VHOST_ALREADY_EXISTS));
            }
          }
          promise.complete(finalResponse);
          LOGGER.info("Successully created vhost : " + Constants.VHOST);
        } else {
          LOGGER.error(" Fail : Creation of vHost failed", ar.cause());
          finalResponse.mergeIn(getResponseJson(500, FAILURE, VHOST_CREATE_ERROR));
          promise.fail(finalResponse.toString());
        }
      });
    }
    return promise.future();
  }

  /**
   * The deletevHost implements the delete virtual host operation.
   *
   * @param request which is a Json object
   * @return response which is a Future object of promise of Json type
   */
  Future<JsonObject> deletevHost(JsonObject request) {
    LOGGER.trace("Info : RabbitClient#deletevHost() started");
    JsonObject finalResponse = new JsonObject();
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String vhost = request.getString("vHost");
      String url = "/api/vhosts/" + encodeValue(vhost);
      webClient.requestAsync(REQUEST_DELETE, url).onComplete(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          if (response != null && !response.equals(" ")) {
            int status = response.statusCode();
            LOGGER.debug("Info : statusCode" + status);
            if (status == HttpStatus.SC_NO_CONTENT) {
              finalResponse.put(Constants.VHOST, vhost);
            } else if (status == HttpStatus.SC_NOT_FOUND) {
              finalResponse.mergeIn(getResponseJson(status, FAILURE, VHOST_NOT_FOUND));
            }
          }
          promise.complete(finalResponse);
          LOGGER.info("Successfully deleted vhost : " + Constants.VHOST);
        } else {
          LOGGER.error("Fail : Deletion of vHost failed -", ar.cause());
          finalResponse.mergeIn(getResponseJson(500, FAILURE, VHOST_DELETE_ERROR));
          promise.fail(finalResponse.toString());
        }
      });
    }

    return promise.future();
  }

  /**
   * The listvHost implements the list of virtual hosts .
   *
   * @param request which is a Json object
   * @return response which is a Future object of promise of Json type
   */
  Future<JsonObject> listvHost(JsonObject request) {
    LOGGER.trace("Info : RabbitClient#listvHost() started");
    JsonObject finalResponse = new JsonObject();
    Promise<JsonObject> promise = Promise.promise();
    if (request != null) {
      JsonArray vhostList = new JsonArray();
      String url = "/api/vhosts";
      webClient.requestAsync(REQUEST_GET, url).onComplete(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          if (response != null && !response.equals(" ")) {
            int status = response.statusCode();
            LOGGER.debug("Info : statusCode" + status);
            if (status == HttpStatus.SC_OK) {
              Buffer body = response.body();
              if (body != null) {
                JsonArray jsonBody = new JsonArray(body.toString());
                jsonBody.forEach(current -> {
                  JsonObject currentJson = new JsonObject(current.toString());
                  String vhostName = currentJson.getString("name");
                  vhostList.add(vhostName);
                });
                if (vhostList != null && !vhostList.isEmpty()) {
                  finalResponse.put(Constants.VHOST, vhostList);
                }
              }
            } else if (status == HttpStatus.SC_NOT_FOUND) {
              finalResponse.mergeIn(getResponseJson(status, FAILURE, VHOST_NOT_FOUND));
            }
          }
          LOGGER.debug("Success : " + finalResponse);
          promise.complete(finalResponse);
        } else {
          LOGGER.error("Fail : Listing of vHost failed - ", ar.cause());
          finalResponse.mergeIn(getResponseJson(500, FAILURE, VHOST_LIST_ERROR));
          promise.fail(finalResponse.toString());
        }
      });
    }
    return promise.future();
  }

  /**
   * The listQueueSubscribers implements the list of bindings for a queue.
   *
   * @param request which is a Json object
   * @return response which is a Future object of promise of Json type
   */
  Future<JsonObject> listQueueSubscribers(JsonObject request, String vhost) {
    LOGGER.trace("Info : RabbitClient#listQueueSubscribers() started");
    JsonObject finalResponse = new JsonObject();
    Promise<JsonObject> promise = Promise.promise();
    if (request != null && !request.isEmpty()) {
      String queueName = request.getString("queueName");
      JsonArray oroutingKeys = new JsonArray();
      String url = "/api/queues/" + vhost + "/" + encodeValue(queueName) + "/bindings";
      webClient.requestAsync(REQUEST_GET, url).onComplete(ar -> {
        if (ar.succeeded()) {
          HttpResponse<Buffer> response = ar.result();
          if (response != null && !response.equals(" ")) {
            int status = response.statusCode();
            LOGGER.debug("Info : statusCode " + status);
            if (status == HttpStatus.SC_OK) {
              Buffer body = response.body();
              if (body != null) {
                JsonArray jsonBody = new JsonArray(body.toString());
                jsonBody.forEach(current -> {
                  JsonObject currentJson = new JsonObject(current.toString());
                  String rkeys = currentJson.getString("routing_key");
                  if (rkeys != null && !rkeys.equalsIgnoreCase(queueName)) {
                    oroutingKeys.add(rkeys);
                  }
                });
                if (oroutingKeys != null && !oroutingKeys.isEmpty()) {
                  finalResponse.put(Constants.ENTITIES, oroutingKeys);
                } else {
                  finalResponse.clear().mergeIn(getResponseJson(HttpStatus.SC_NOT_FOUND,
                          FAILURE, QUEUE_DOES_NOT_EXISTS));
                }
              }
            } else if (status == HttpStatus.SC_NOT_FOUND) {
              finalResponse.clear()
                      .mergeIn(getResponseJson(status, FAILURE, QUEUE_DOES_NOT_EXISTS));
            }
          }
          LOGGER.debug("Info : " + finalResponse);
          promise.complete(finalResponse);
        } else {
          LOGGER.error("Error : Listing of Queue failed - " + ar.cause());
          finalResponse.mergeIn(getResponseJson(500, FAILURE, QUEUE_LIST_ERROR));
          promise.fail(finalResponse.toString());
        }
      });
    }
    return promise.future();
  }

  private class AdaptorResultContainer {
    public String apiKey;
    public String id;
    public String resourceServer;
    public String userid;
    public String adaptorId;
    public String vhost;
    public boolean isExchnageCreated;

  }

  Future<JsonObject> deleteAdapter(JsonObject json, String vhost) {
    LOGGER.trace("Info : RabbitClient#deleteAdapter() started");
    Promise<JsonObject> promise = Promise.promise();
    JsonObject finalResponse = new JsonObject();
    Future<JsonObject> result = getExchange(json, vhost);
    result.onComplete(resultHandler -> {
      if (resultHandler.succeeded()) {
        int status = resultHandler.result().getInteger("type");
        if (status == 200) {
          String exchangeID = json.getString("id");
          String userId = json.getString("userid");
          String url = "/api/exchanges/" + vhost + "/" + encodeValue(exchangeID);
          webClient.requestAsync(REQUEST_DELETE, url).onComplete(rh -> {
            if (rh.succeeded()) {
              LOGGER.debug("Info : " + exchangeID + " adaptor deleted successfully");
              finalResponse.mergeIn(getResponseJson(200, "success", "adaptor deleted"));
              Future.future(
                      fu -> updateUserPermissions(vhost, userId, PermissionOpType.DELETE_WRITE,
                              exchangeID));
            } else if (rh.failed()) {
              finalResponse.clear()
                      .mergeIn(getResponseJson(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Adaptor deleted",
                              rh.cause().toString()));
              LOGGER.error("Error : Adaptor deletion failed cause - " + rh.cause());
              promise.fail(finalResponse.toString());
            } else {
              LOGGER.error("Error : Something wrong in deleting adaptor" + rh.cause());
              finalResponse.mergeIn(getResponseJson(400, "bad request", "nothing to delete"));
              promise.fail(finalResponse.toString());
            }
            promise.tryComplete(finalResponse);
          });

        } else if (status == 404) { // exchange not found
          finalResponse.clear().mergeIn(
                  getResponseJson(status, "not found", resultHandler.result().getString("detail")));
          LOGGER.error("Error : Exchange not found cause ");
          promise.fail(finalResponse.toString());
        } else { // some other issue
          LOGGER.error("Error : Bad request");
          finalResponse.mergeIn(getResponseJson(400, "bad request", "nothing to delete"));
          promise.fail(finalResponse.toString());
        }
      }
      if (resultHandler.failed()) {
        LOGGER.error("Error : deleteAdaptor - resultHandler failed : " + resultHandler.cause());
        finalResponse
                .mergeIn(getResponseJson(INTERNAL_ERROR_CODE, "bad request", "nothing to delete"));
        promise.fail(finalResponse.toString());

      }
    });
    return promise.future();
  }






  Future<JsonObject> resetPasswordInRMQ(String userid, String password) {
    LOGGER.trace("Info : RabbitClient#resetPassword() started");
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    JsonObject arg = new JsonObject();
    arg.put(PASSWORD, password);
    arg.put(TAGS, NONE);
    String url = "/api/users/" + userid;
    webClient.requestAsync(REQUEST_PUT, url, arg).onComplete(ar -> {
      if (ar.succeeded()) {
        if (ar.result().statusCode() == HttpStatus.SC_NO_CONTENT) {
          response.put(userid, userid);
          response.put(PASSWORD, password);
          LOGGER.debug("user password changed");
          promise.complete(response);
        } else {
          LOGGER.error("Error :reset pwd method failed", ar.cause());
          response.put(FAILURE, NETWORK_ISSUE);
          promise.fail(response.toString());
        }
      } else {
        LOGGER.error("User creation failed using mgmt API :", ar.cause());
        response.put(FAILURE, CHECK_CREDENTIALS);
        promise.fail(response.toString());
      }
    });
    return promise.future();
  }



  /**
   * set topic permissions.
   *
   * @param vhost which is a String
   * @param adaptorID which is a String
   * @param userID which is a String
   * @return response which is a Future object of promise of Json type
   **/
  /**
   *
   * changed the access modifier to default as
   * setTopicPermissions is not being called anywhere
   */
  Future<JsonObject> setTopicPermissions(String vhost, String adaptorID, String userID) {
    LOGGER.trace("Info : RabbitClient#setTopicPermissions() started");
    String url = "/api/permissions/" + vhost + "/" + encodeValue(userID);
    JsonObject param = new JsonObject();
    // set all mandatory fields
    param.put(EXCHANGE, adaptorID);
    param.put(WRITE, ALLOW);
    param.put(READ, DENY);
    param.put(CONFIGURE, DENY);

    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    webClient.requestAsync(REQUEST_PUT, url, param).onComplete(result -> {
      if (result.succeeded()) {
        /* Check if request was a success */
        if (result.result().statusCode() == HttpStatus.SC_CREATED) {
          response.mergeIn(
                  getResponseJson(SUCCESS_CODE, TOPIC_PERMISSION, TOPIC_PERMISSION_SET_SUCCESS));
          LOGGER.debug("Success : Topic permission set");
          promise.complete(response);
        } else if (result.result()
                .statusCode() == HttpStatus.SC_NO_CONTENT) { /* Check if request was already served */
          response.mergeIn(
                  getResponseJson(SUCCESS_CODE, TOPIC_PERMISSION, TOPIC_PERMISSION_ALREADY_SET));
          promise.complete(response);
        } else { /* Check if request has an error */
          LOGGER.error(
                  "Error : error in setting topic permissions" + result.result().statusMessage());
          response.mergeIn(
                  getResponseJson(INTERNAL_ERROR_CODE, TOPIC_PERMISSION, TOPIC_PERMISSION_SET_ERROR));
          promise.fail(response.toString());
        }
      } else { /* Check if request has an error */
        LOGGER.error("Error : error in setting topic permission : " + result.cause());
        response.mergeIn(
                getResponseJson(INTERNAL_ERROR_CODE, TOPIC_PERMISSION, TOPIC_PERMISSION_SET_ERROR));
        promise.fail(response.toString());
      }
    });
    return promise.future();
  }

  /**
   * set vhost permissions for given userName.
   *
   * @param shaUsername which is a String
   * @param vhost which is a String
   * @return response which is a Future object of promise of Json type
   **/
   Future<JsonObject> setVhostPermissions(String shaUsername, String vhost) {
    LOGGER.trace("Info : RabbitClient#setVhostPermissions() started");
    /* Construct URL to use */
    String url = "/api/permissions/" + vhost + "/" + encodeValue(shaUsername);
    JsonObject vhostPermissions = new JsonObject();
    // all keys are mandatory. empty strings used for configure,read as not
    // permitted.
    vhostPermissions.put(CONFIGURE, DENY);
    vhostPermissions.put(WRITE, NONE);
    vhostPermissions.put(READ, NONE);
    Promise<JsonObject> promise = Promise.promise();
    /* Construct a response object */
    JsonObject vhostPermissionResponse = new JsonObject();
    webClient.requestAsync(REQUEST_PUT, url, vhostPermissions).onComplete(handler -> {
      if (handler.succeeded()) {
        /* Check if permission was set */
        if (handler.result().statusCode() == HttpStatus.SC_CREATED) {
          LOGGER.debug("Success :write permission set for user [ " + shaUsername + " ] in vHost [ "
                  + vhost + "]");
          vhostPermissionResponse
                  .mergeIn(getResponseJson(SUCCESS_CODE, VHOST_PERMISSIONS, VHOST_PERMISSIONS_WRITE));
          promise.complete(vhostPermissionResponse);
        } else {
          LOGGER.error("Error : error in write permission set for user [ " + shaUsername
                  + " ] in vHost [ " + vhost + " ]");
          vhostPermissionResponse.mergeIn(
                  getResponseJson(INTERNAL_ERROR_CODE, VHOST_PERMISSIONS, VHOST_PERMISSION_SET_ERROR));
          promise.fail(vhostPermissions.toString());
        }
      } else {
        /* Check if request has an error */
        LOGGER.error("Error : error in write permission set for user [ " + shaUsername
                + " ] in vHost [ " + vhost + " ]");
        vhostPermissionResponse.mergeIn(
                getResponseJson(INTERNAL_ERROR_CODE, VHOST_PERMISSIONS, VHOST_PERMISSION_SET_ERROR));
        promise.fail(vhostPermissions.toString());
      }
    });
    return promise.future();
  }

  /**
   * Helper method which bind registered exchange with predefined queues
   *
   * @param adaptorID which is a String object
   *
   * @return response which is a Future object of promise of Json type
   */
  Future<JsonObject> queueBinding(String adaptorID, String vhost) {
    LOGGER.trace("RabbitClient#queueBinding() method started");
    Promise<JsonObject> promise = Promise.promise();
    String topics;

    if (isGroupId(adaptorID)) {
      topics = adaptorID + DATA_WILDCARD_ROUTINGKEY;
    } else {
      topics = adaptorID;
    }

    bindQueue(QUEUE_DATA, adaptorID, topics, vhost)
            .compose(databaseResult -> bindQueue(REDIS_LATEST, adaptorID, topics, vhost))
            .compose(queueDataResult -> bindQueue(QUEUE_ADAPTOR_LOGS, adaptorID, adaptorID + HEARTBEAT,
                    vhost))
            .compose(heartBeatResult -> bindQueue(QUEUE_ADAPTOR_LOGS, adaptorID, adaptorID + DATA_ISSUE,
                    vhost))
            .compose(dataIssueResult -> bindQueue(QUEUE_ADAPTOR_LOGS, adaptorID,
                    adaptorID + DOWNSTREAM_ISSUE, vhost))
            .onSuccess(successHandler -> {
              JsonObject response = new JsonObject();
              response.mergeIn(getResponseJson(SUCCESS_CODE, "Queue_Database",
                      QUEUE_DATA + " queue bound to " + adaptorID));
              LOGGER.debug("Success : " + response);
              promise.complete(response);
            }).onFailure(failureHandler -> {
              LOGGER.error("Error : queue bind error : " + failureHandler.getCause().toString());
              JsonObject response = getResponseJson(INTERNAL_ERROR_CODE, ERROR, QUEUE_BIND_ERROR);
              promise.fail(response.toString());
            });
    return promise.future();
  }

  Future<Void> bindQueue(String queue, String adaptorID, String topics, String vhost) {
    LOGGER.trace("Info : RabbitClient#bindQueue() started");
    LOGGER.debug("Info : data : " + queue + " adaptorID : " + adaptorID + " topics : " + topics);
    Promise<Void> promise = Promise.promise();
    String url =
            "/api/bindings/" + vhost + "/e/" + encodeValue(adaptorID) + "/q/" + encodeValue(queue);
    JsonObject bindRequest = new JsonObject();
    bindRequest.put("routing_key", topics);

    webClient.requestAsync(REQUEST_POST, url, bindRequest).onComplete(handler -> {
      if (handler.succeeded()) {
        promise.complete();
      } else {
        LOGGER.error("Error : Queue" + queue + " binding error : ", handler.cause());
        promise.fail(handler.cause());
      }
    });
    return promise.future();
  }

  Future<JsonObject> getUserPermissions(String userId) {
    LOGGER.trace("Info : RabbitClient#getUserpermissions() started");
    Promise<JsonObject> promise = Promise.promise();
    String url = "/api/users/" + encodeValue(userId) + "/permissions";
    webClient.requestAsync(REQUEST_GET, url).onComplete(handler -> {
      if (handler.succeeded()) {
        HttpResponse<Buffer> rmqResponse = handler.result();

        if (rmqResponse.statusCode() == HttpStatus.SC_OK) {
          JsonArray permissionArray = new JsonArray(rmqResponse.body().toString());
          promise.complete(permissionArray.getJsonObject(0));
        } else if (handler.result().statusCode() == HttpStatus.SC_NOT_FOUND) {
          Response response = new Response.Builder()
                  .withStatus(HttpStatus.SC_NOT_FOUND)
                  .withTitle(ResponseUrn.BAD_REQUEST_URN.getUrn())
                  .withDetail("user not exist.")
                  .withUrn(ResponseUrn.BAD_REQUEST_URN.getUrn())
                  .build();
          promise.fail(response.toString());
        } else {
          LOGGER.error(handler.cause());
          LOGGER.error(handler.result());
          Response response = new Response.Builder()
                  .withStatus(rmqResponse.statusCode())
                  .withTitle(ResponseUrn.BAD_REQUEST_URN.getUrn())
                  .withDetail("problem while getting user permissions")
                  .withUrn(ResponseUrn.BAD_REQUEST_URN.getUrn())
                  .build();
          promise.fail(response.toString());
        }
      } else {
        Response response = new Response.Builder()
                .withStatus(HttpStatus.SC_BAD_REQUEST)
                .withTitle(ResponseUrn.BAD_REQUEST_URN.getUrn())
                .withDetail(handler.cause().getLocalizedMessage())
                .withUrn(ResponseUrn.BAD_REQUEST_URN.getUrn())
                .build();
        promise.fail(response.toString());
      }
    });
    return promise.future();
  }

  Future<JsonObject> updateUserPermissions(String vHost, String userId, PermissionOpType type,
                                           String resourceId) {
    Promise<JsonObject> promise = Promise.promise();
    getUserPermissions(userId).onComplete(handler -> {
      if (handler.succeeded()) {
        String url = "/api/permissions/" + vHost + "/" + encodeValue(userId);
        JsonObject existingPermissions = handler.result();

        JsonObject updatedPermission = getUpdatedPermission(existingPermissions, type, resourceId);

        LOGGER.debug("updated permission json :" + updatedPermission);
        webClient.requestAsync(REQUEST_PUT, url, updatedPermission)
                .onComplete(updatePermissionHandler -> {
                  if (updatePermissionHandler.succeeded()) {
                    HttpResponse<Buffer> rmqResponse = updatePermissionHandler.result();
                    if (rmqResponse.statusCode() == HttpStatus.SC_NO_CONTENT) {
                      Response response = new Response.Builder()
                              .withStatus(HttpStatus.SC_NO_CONTENT)
                              .withTitle(ResponseUrn.SUCCESS_URN.getUrn())
                              .withDetail("Permission updated successfully.")
                              .withUrn(ResponseUrn.SUCCESS_URN.getUrn())
                              .build();
                      promise.complete(response.toJson());
                    }
                    else if (rmqResponse.statusCode() == HttpStatus.SC_CREATED) {
                      Response response = new Response.Builder()
                              .withStatus(HttpStatus.SC_CREATED)
                              .withTitle(ResponseUrn.SUCCESS_URN.getUrn())
                              .withDetail("Permission updated successfully.")
                              .withUrn(ResponseUrn.SUCCESS_URN.getUrn())
                              .build();
                      promise.complete(response.toJson());
                    }
                    else {
                      Response response = new Response.Builder()
                              .withStatus(rmqResponse.statusCode())
                              .withTitle(ResponseUrn.BAD_REQUEST_URN.getUrn())
                              .withDetail(rmqResponse.statusMessage())
                              .withUrn(ResponseUrn.BAD_REQUEST_URN.getUrn())
                              .build();
                      promise.fail(response.toString());
                    }
                  } else {
                    Response response = new Response.Builder()
                            .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                            .withTitle(ResponseUrn.BAD_REQUEST_URN.getUrn())
                            .withDetail(updatePermissionHandler.cause().getMessage())
                            .withUrn(ResponseUrn.BAD_REQUEST_URN.getUrn())
                            .build();
                    promise.fail(response.toString());
                  }
                });
      } else {
        promise.fail(handler.cause().getMessage());
      }
    });
    return promise.future();
  }

  private JsonObject getUpdatedPermission(JsonObject permissionsJson, PermissionOpType type,
                                          String resourceId) {
    LOGGER.debug("existing permissions : " + permissionsJson);
    switch (type) {
      case ADD_READ:
      case ADD_WRITE: {
        StringBuilder permission = new StringBuilder(permissionsJson.getString(type.permission));
        LOGGER.debug("permissions : " + permission.toString());
        if (permission.length() != 0 && permission.indexOf(".*") != -1) {
          permission.deleteCharAt(0).deleteCharAt(0);
        }
        if (permission.length() != 0) {
          permission.append("|").append(resourceId);
        } else {
          permission.append(resourceId);
        }

        permissionsJson.put(type.permission, permission.toString());
        break;
      }
      case DELETE_READ:
      case DELETE_WRITE: {
        StringBuilder permission = new StringBuilder(permissionsJson.getString(type.permission));
        String[] permissionsArray = permission.toString().split("\\|");
        if (permissionsArray.length > 0) {
          Stream<String> stream = Arrays.stream(permissionsArray);
          String updatedPermission = stream
                  .filter(item -> !item.equals(resourceId))
                  .collect(Collectors.joining("|"));
          permissionsJson.put(type.permission, updatedPermission);
        }
        break;
      }
    }
    return permissionsJson;
  }

  public RabbitMQClient getRabbitMQClient() {
    return this.client;
  }
}
