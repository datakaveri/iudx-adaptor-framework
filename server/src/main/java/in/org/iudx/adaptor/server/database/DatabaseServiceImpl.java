package in.org.iudx.adaptor.server.database;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import static in.org.iudx.adaptor.server.util.Constants.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DatabaseServiceImpl implements DatabaseService {

  private static final Logger LOGGER = LogManager.getLogger(DatabaseServiceImpl.class);
  private PostgresClient client;

  public DatabaseServiceImpl(PostgresClient postgresClient) {
    this.client = postgresClient;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService getAdaptor(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {

    JsonArray response = new JsonArray();
    String username = request.getString(USERNAME);
    String id = request.getString(ADAPTOR_ID);
        
    String query;
    if(id != null) {
      query = GET_ONE_ADAPTOR.replace("$1", username).replace("$2", id);
    } else {
      query = GET_ALL_ADAPTOR.replace("$1", username);
    }

    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        RowSet<Row> result = pgHandler.result();
        for (Row row : result) {
          JsonObject rowJson = new JsonObject();
          JsonObject tempJson = row.toJson();
          JsonObject configData = tempJson.getJsonObject(DATA);
          
          rowJson.put(ID, tempJson.getValue("adaptor_id"))
                 .put(NAME, configData.getString(NAME))
                 .put(JAR_ID, tempJson.getString("jar_id"))
                 .put(SCHEDULE_PATTERN, configData.getString(SCHEDULE_PATTERN,null))
                 .put(JOB_ID, tempJson.getString("job_id"))
                 .put(LASTSEEN, tempJson.getString(TIMESTAMP))
                 .put(STATUS, tempJson.getString(STATUS))
                 .put(ADAPTOR_TYPE, tempJson.getString("adaptor_type"));
          response.add(rowJson);
        }
        
        handler.handle(
            Future.succeededFuture(
                new JsonObject().put(STATUS, SUCCESS).put(ADAPTORS, response)));
      } else {
        LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    return this;
  }



  @Override
  public DatabaseService getRuleSource(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {

    JsonArray response = new JsonArray();
    String username = request.getString(USERNAME);
    String adaptorId = request.getString(ADAPTOR_ID);
        
    LOGGER.debug("Getting rule source");
    String query;
    query = GET_RULE_SOURCE_FROM_ADAPTOR_ID.replace("$1", username)
                                      .replace("$2", adaptorId);
    LOGGER.debug("Query is   " + query);
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        RowSet<Row> result = pgHandler.result();
        LOGGER.debug("Got results");
        for (Row row : result) {
          JsonObject rowJson = new JsonObject();
          JsonObject tempJson = row.toJson();
          LOGGER.debug(tempJson.toString());
          
          rowJson.put(ID, tempJson.getValue("adaptor_id"))
                 .put(SOURCE_ID, tempJson.getString("source_id"))
                 .put(EXCHANGE_NAME, tempJson.getString("ruleexchange"))
                 .put(QUEUE_NAME, tempJson.getString("rulequeue"))
                 .put(STATUS, tempJson.getString(STATUS));
          response.add(rowJson);
        }
        
        handler.handle(
            Future.succeededFuture(
                new JsonObject().put(STATUS, SUCCESS).put(ADAPTORS, response)));
      } else {
        LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    return this;
  }


  @Override
  public DatabaseService getRuleSources(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {

    JsonArray response = new JsonArray();
    String username = request.getString(USERNAME);
        
    String query;
    query = GET_ALL_RULE_SOURCES.replace("$1", username);
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        RowSet<Row> result = pgHandler.result();
        for (Row row : result) {
          JsonObject rowJson = new JsonObject();
          JsonObject tempJson = row.toJson();
          
          rowJson.put(ID, tempJson.getValue("adaptor_id"))
                 .put(SOURCE_ID, tempJson.getString("source_id"))
                 .put(EXCHANGE_NAME, tempJson.getString("ruleexchange"))
                 .put(QUEUE_NAME, tempJson.getString("rulequeue"))
                 .put(STATUS, tempJson.getString(STATUS));
          response.add(rowJson);
        }
        
        handler.handle(
            Future.succeededFuture(
                new JsonObject().put(STATUS, SUCCESS).put(ADAPTORS, response)));
      } else {
        LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    return this;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService registerUser(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    
    String mode = request.getString(MODE);
    String query = null;
    if(mode.equals("POST")) {
      query = REGISTER_USER.replace("$1", request.getString(USERNAME))
                           .replace("$2", request.getString(PASSWORD))
                           .replace("$3", "active");
      
    } else if(mode.equals("PUT")) {
      query = UPDATE_USER_PASSWORD.replace("$1", request.getString(USERNAME))
                                  .replace("$2", request.getString(PASSWORD));
      
      if(request.containsKey(STATUS)) {
        query = UPDATE_USER.replace("$1", request.getString(USERNAME))
                           .replace("$2", request.getString(PASSWORD))
                           .replace("$3", request.getString(STATUS));
      }
      
    } else if (mode.equals(STATUS)) {
      query = UPDATE_USER_STATUS.replace("$1", request.getString(USERNAME))
                                .replace("$2", request.getString(STATUS));
    }
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        if(pgHandler.result().rowCount() == 1) {
          LOGGER.debug("Info: Database query succeeded");
          handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS))); 
        } else {
          LOGGER.error("Info: Database query failed; User not registered");
          handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
        }
      } else {
        LOGGER.error("Info: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    
    return this;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService getAdaptorUser(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    
    JsonArray response = new JsonArray();
    String query = null;
    String id = request.getString(ID,"");
    
    if(id != null && !id.isBlank()) {
      query = GET_USER.replace("$1", request.getString(ID));
    } else {
      query = GET_USERS;
    }
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        RowSet<Row> result = pgHandler.result();
        for (Row row : result) {
          response.add(row.toJson());
        }
        
        handler.handle(
            Future.succeededFuture(
                new JsonObject().put(STATUS, SUCCESS).put("users", response)));
      } else {
        LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService authenticateUser(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    
    JsonArray response = new JsonArray();
    String query = AUTHENTICATE_USER
                      .replace("$1", request.getString(USERNAME))
                      .replace("$2",request.getString(PASSWORD));
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        LOGGER.debug("Info: Database query succeeded");
        RowSet<Row> result = pgHandler.result();
        for (Row row : result) {
          response.add(row.toJson());
        }

        JsonObject queryRes = response.getJsonObject(0);
        if (queryRes.containsKey(EXISTS) && queryRes.getBoolean(EXISTS) == true) {
          handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
        } else {
          handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, FAILED)));
        }
      } else {
        LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService createAdaptor(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    System.out.println("Creating adaptor");
    
    String username = request.getString(USERNAME);
    String adaptorId = request.getString(ADAPTOR_ID);
    String data = request.getString(DATA).replace("'", "\\\"");
    String adaptorType = request.getString(ADAPTOR_TYPE);
    
    String query = CREATE_ADAPTOR
                      .replace("$1", adaptorId)
                      .replace("$3", username)
                      .replace("$4", COMPILING)
                      .replace("$2",data)
                      .replace("$5", adaptorType);

    LOGGER.debug(query);
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        if (adaptorType.equals(ADAPTOR_RULE)) {
          String sourceId = request.getString(SOURCE_ID);
          String ruleQuery = CREATE_RULESOURCE.replace("$1", adaptorId)
                              .replace("$5", username)
                              .replace("$2", sourceId)
                              .replace("$3", adaptorId+"_exchange")
                              .replace("$4", adaptorId+"_queue");
          client.executeAsync(ruleQuery).onComplete(pgHandler1 -> {
            LOGGER.debug("Info: Database query succeeded");
            handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
            return;
          });
        }
        LOGGER.debug("Info: Database query succeeded");
        handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
        return;

      } else {
        LOGGER.error("Info: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });

    return this;
  }



  @Override
  public DatabaseService createRule(JsonObject request,
      Handler<AsyncResult<JsonObject>> handler) {
    
    LOGGER.debug("Creating rule in db");
    LOGGER.debug(request.toString());

    String username = request.getString(USERNAME);
    String adaptorId = request.getString(ADAPTOR_ID);
    String sqlQuery = request.getString(SQL_QUERY);
    int windowMinutes = request.getInteger(WINDOW_MINUTES);
    String ruleType = request.getString(RULE_TYPE);
    String exchangeName = request.getString(EXCHANGE_NAME);
    String queueName = request.getString(QUEUE_NAME);
    String routingKey = request.getString(ROUTING_KEY);
    String ruleName = request.getString(RULE_NAME);
    
    String query = CREATE_RULE
                      .replace("$1", adaptorId)
                      .replace("$2", exchangeName)
                      .replace("$3", queueName)
                      .replace("$4", routingKey)
                      .replace("$5", sqlQuery)
                      .replace("$6", String.valueOf(windowMinutes))
                      .replace("$7", ruleType)
                      .replace("$8", username)
                      .replace("$9", ruleName);

    LOGGER.debug("QUery is " + query);

    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        LOGGER.debug("Info: Database query succeeded");
        int uuid = -1;
        // TODO: Ensure only one row is returned
        for(Row row: pgHandler.result()) {
          uuid = row.getInteger(0);
        }
        handler.handle(Future.succeededFuture(new JsonObject()
                                              .put(STATUS, SUCCESS)
                                                .put("uuid", uuid)));
      } else {
        LOGGER.error("Info: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });

    return this;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService updateComplex(String query,
      Handler<AsyncResult<JsonObject>> handler) {
    
    LOGGER.debug("Info: Handling complex queries");
    
    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        LOGGER.debug("Info: Database query succeeded");
        handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
      } else {
        LOGGER.error("Info: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService deleteAdaptor(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
    
    String username = request.getString(USERNAME);
    String id = request.getString(ADAPTOR_ID);
    
    String getQuery = GET_ONE_ADAPTOR.replace("$1", username).replace("$2", id);
    String deleteQuery = DELETE_ADAPTOR.replace("$1", id);
    
    LOGGER.debug("Info: Handling delete queries");
    
    client.executeAsync(getQuery).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        JsonObject tempJson = new JsonObject();
        RowSet<Row> result = pgHandler.result();
        if(result.size() == 1) {
          for(Row row: result) {
            tempJson.put(JAR_ID, row.toJson().getString("jar_id"));
            tempJson.put(STATUS, row.toJson().getString(STATUS));
            tempJson.put(DATA, row.toJson().getJsonObject(DATA));
          }
          
          String status = tempJson.getString(STATUS);
          String jarId = tempJson.getString(JAR_ID);
          if((jarId != null && !jarId.isBlank()) || !status.equals(COMPILING)) {
            if(status == null || !status.equals(RUNNING)) {
              client.executeAsync(deleteQuery).onComplete(deleteHandler ->{
                if(deleteHandler.succeeded()) {
                  LOGGER.debug("Info: Database query succeeded");
                  handler.handle(Future.succeededFuture(tempJson.put(STATUS, SUCCESS)));
                } else {
                  LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
                  handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
                }
              }); 
            } else {
              LOGGER.error("Error: Already running instance; stop it before deletion");
              handler.handle(Future.failedFuture(new JsonObject().put(STATUS, ALREADY_RUNNING).toString()));
            }
          } else {
            LOGGER.error("Error: Codegen in process; Jar not submitted");
            handler.handle(Future.failedFuture(new JsonObject().put(STATUS, INCOMPLETE_CODEGEN).toString()));
          }
        } else {
          LOGGER.error("Error: Unable to delete");
          handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
        }
      } else {
        LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public DatabaseService syncAdaptorJob(String query, Handler<AsyncResult<JsonObject>> handler) {
    
    JsonArray response = new JsonArray();

    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        RowSet<Row> result = pgHandler.result();
        for (Row row : result) {
          response.add(row.toJson().getString("job_id"));
        }
        
        handler.handle(
            Future.succeededFuture(
                new JsonObject().put(JOBS, response)));
      } else {
        LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });

    return null;
  }

  @Override
  public DatabaseService getRulesByAdaptor(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {

    JsonArray response = new JsonArray();
    String username = request.getString(USERNAME);
    String adaptorId = request.getString(ADAPTOR_ID);

    LOGGER.debug("Getting rules");
    String query;
    query = GET_RULES_FROM_ADAPTOR_ID.replace("$1", username)
            .replace("$2", adaptorId);

    LOGGER.debug("Query is   " + query);

    client.executeAsync(query).onComplete(pgHandler -> {
      if (pgHandler.succeeded()) {
        RowSet<Row> result = pgHandler.result();
        LOGGER.debug("Got results");
        for (Row row : result) {
          JsonObject rowJson = new JsonObject();
          JsonObject tempJson = row.toJson();
          LOGGER.debug(tempJson.toString());

          rowJson.put(ID, tempJson.getValue("id"))
                 .put(RULE_NAME, tempJson.getString("rule_name"))
                 .put(ADAPTOR_ID, tempJson.getString("adaptor_id"))
                 .put(EXCHANGE_NAME, tempJson.getString("exchangename"))
                 .put(QUEUE_NAME, tempJson.getString("queuename"))
                 .put(SQL_QUERY, tempJson.getString("sqlquery"))
                 .put("createdAt", tempJson.getString(TIMESTAMP));
          response.add(rowJson);
        }

        handler.handle(
                Future.succeededFuture(
                        new JsonObject().put(STATUS, SUCCESS).put(RULES, response)));
      } else {
        LOGGER.error("Error: Database query failed; " + pgHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    return this;
  }

  @Override
  public DatabaseService deleteRule(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
    String adaptorId = request.getString(ADAPTOR_ID);
    String ruleId = request.getString(RULE_ID);

    String deleteQuery = DELETE_RULE.replace("$1", adaptorId).replace("$2", ruleId);

    LOGGER.debug("Info: Handling rule delete queries");

    client.executeAsync(deleteQuery).onComplete(deleteHandler ->{
      if(deleteHandler.succeeded()) {
        LOGGER.debug("Info: Database query rule delete succeeded");
        handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
      } else {
        LOGGER.error("Error: Database rule delete query failed; " + deleteHandler.cause().getMessage());
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
      }
    });
    return this;
  }
}
