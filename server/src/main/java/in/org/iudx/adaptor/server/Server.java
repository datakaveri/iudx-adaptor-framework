package in.org.iudx.adaptor.server;

import in.org.iudx.adaptor.datatypes.Rule;
import in.org.iudx.adaptor.datatypes.Rule.RuleType;
import in.org.iudx.adaptor.server.codegeninit.CodegenInitService;
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.databroker.RabbitClient;
import in.org.iudx.adaptor.server.databroker.RabbitWebClient;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import in.org.iudx.adaptor.server.specEndpoints.InputSpecEndpoint;
import in.org.iudx.adaptor.server.specEndpoints.ParseSpecEndpoint;
import in.org.iudx.adaptor.server.specEndpoints.TransformSpecEndpoint;
import in.org.iudx.adaptor.server.util.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.FileSystems;
import java.util.Set;

import static in.org.iudx.adaptor.server.util.Constants.*;

/**
 * The Adaptor API Server API Verticle.
 *
 * <h1>Adaptor Server API Verticle</h1>
 */

public class Server extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(Server.class);
  private HttpServer server;
  private FlinkClientService flinkClient;
  @SuppressWarnings("unused")
  private Router router;
  private String keystore;
  private String keystorePassword;
  private boolean isSsl;
  private int port;
  private Validator validator;
  private Validator onboardingValidator;
  private JobScheduler jobScheduler;
  private CodegenInitService codegenInit;
  private JsonObject authCred;
  private String quartzPropertiesPath;
  private String jarOutPath;
  private DatabaseService databaseService;
  private DbFlinkSync dbFlinkSync;
  private RabbitClient rmqClient;
  private RabbitWebClient rmqWebClient;
  private String rmqHost;
  private int rmqMgmtPort;
  private int rmqPort;
  private String rmqVHost;
  private String rmqUName;
  private String rmqPassword;
  private WebClientOptions webConfig;
  private RabbitMQOptions rmqConfig;

  @Override
  public void start() throws Exception {
    router = Router.router(vertx);

    keystore = config().getString(KEYSTORE_PATH);
    keystorePassword = config().getString(KEYSTORE_PASSWORD);
    isSsl = config().getBoolean(IS_SSL);
    port = config().getInteger(PORT);
    authCred = config().getJsonObject("adminAuth");
    quartzPropertiesPath = config().getString(QUARTZ_PROPERTIES_PATH);
    jarOutPath = config().getString(JAR_OUT_PATH);


    rmqHost = config().getString(RMQ_HOST);
    rmqPort = config().getInteger(RMQ_PORT);
    rmqMgmtPort = config().getInteger(RMQ_MANAGEMENT_PORT);
    rmqVHost = config().getString(RMQ_VHOST);
    rmqUName = config().getString(RMQ_USERNAME);
    rmqPassword = config().getString(RMQ_PASSWORD);

    webConfig = new WebClientOptions();
    webConfig.setKeepAlive(true);
    webConfig.setConnectTimeout(86400000);
    webConfig.setDefaultHost(rmqHost);
    webConfig.setDefaultPort(rmqMgmtPort);
    webConfig.setKeepAliveTimeout(86400000);
    /** TODO **/
    webConfig.setSsl(false);

    JsonObject propObj = new JsonObject();

    propObj.put("userName", rmqUName);
    propObj.put("password", rmqPassword);
    propObj.put("vHost", rmqVHost);
    if (rmqVHost.equals("%2F")) {
      propObj.put("vHost", "/");
    }


    rmqConfig = new RabbitMQOptions();
    rmqConfig.setUser(rmqUName);
    rmqConfig.setPassword(rmqPassword);
    rmqConfig.setHost(rmqHost);
    rmqConfig.setPort(rmqPort);
    rmqConfig.setVirtualHost(rmqVHost);
    if (rmqVHost.equals("%2F")) {
      rmqConfig.setVirtualHost("/");
    }
    rmqConfig.setAutomaticRecoveryEnabled(true);


    rmqWebClient = new RabbitWebClient(vertx, webConfig, propObj);
    rmqClient =
            new RabbitClient(vertx, rmqConfig, rmqWebClient);

    databaseService = DatabaseService.createProxy(vertx, DATABASE_SERVICE_ADDRESS);
    HttpServerOptions serverOptions = new HttpServerOptions();

    InputSpecEndpoint ise = new InputSpecEndpoint();
    ParseSpecEndpoint pse = new ParseSpecEndpoint();


    if (isSsl) {
      serverOptions.setSsl(true)
              .setKeyStoreOptions(new JksOptions().setPath(keystore).setPassword(keystorePassword));
    } else {
      serverOptions.setSsl(false);
    }
    serverOptions.setCompressionSupported(true).setCompressionLevel(5);
    /** Instantiate this server */
    server = vertx.createHttpServer(serverOptions);

    /**
     * Routes - Defines the routes and callbacks
     */
    Router router = Router.router(vertx);

    /* Route for enabling file upload with dir */
    router.route().handler(
            BodyHandler.create()
                    .setUploadsDirectory(JAR_OUT_PATH)
                    .setDeleteUploadedFilesOnEnd(true));

    router.route().handler(
            CorsHandler.create("*")
                    .allowedHeaders(ALLOWED_HEADERS)
                    .allowedMethods(ALLOWED_METHODS));

    /* Sumbit Jar Route */
    router.post(JAR_ROUTE)
            .consumes(Constants.MULTIPART_FORM_DATA)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              submitJarHandler(routingContext);
            });

    /* Get all the Jar */
    router.get(JAR_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getJarsHandler(routingContext);
            });

    /* Get a Single Jar plan */
    router.get(GET_JAR_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getJarsHandler(routingContext);
            });

    /* Delete all the submitted Jar */
    router.delete(JAR_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              deleteJarsHandler(routingContext);
            });

    /* Delete a single jar */
    router.delete(GET_JAR_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              deleteJarsHandler(routingContext);
            });

    /* Route for running a Jar */
    router.post(JOB_RUN_ROUTE)
            .consumes(MIME_APPLICATION_JSON)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              runJobHandler(routingContext);
            });

    /* Get the all running/completed jobs */
    router.get(JOBS_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getJobsHandler(routingContext);
            });

    /* Get the details of single job */
    router.get(JOB_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getJobsHandler(routingContext);
            });

    /* Get the all logs file */
    router.get(LOGS_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getLogsHandler(routingContext);
            });

    /* Get the single log file*/
    router.get(LOG_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getLogsHandler(routingContext);
            });

    /* Get all the scheduled Jobs */
    router.get(SCHEDULER_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            //.consumes(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getAllScheduledJobs(routingContext);
            });

    /* Schedule a quartz Job */
    router.post(SCHEDULER_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            //.consumes(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              scheduledJobs(routingContext);
            });

    /*Delete all the scheduled jobs*/
    router.delete(SCHEDULER_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              deleteScheduledJobs(routingContext);
            });

    /*Delete specific scheduled job*/
    router.delete(DELETE_SCHEDULER_JOB)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              deleteScheduledJobs(routingContext);
            });

    /*Adaptor, config file processing*/
    router.post(ADAPTOR_ROUTE)
            .consumes(MIME_APPLICATION_JSON)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
                createAdaptorHandler(routingContext);
            });


    /* New rule */
    router.post(RULE_ROUTE)
            .consumes(MIME_APPLICATION_JSON)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              newRuleHander(routingContext);
            });


    /* Running a Adaptor */
    router.post(ADAPTOR_START_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              runAdaptorHandler(routingContext);
            });

    /* Stopping a Adaptor */
    router.post(ADAPTOR_STOP_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              stopAdaptorHandler(routingContext);
            });

    /*Get a (one) adaptor status*/
    router.get(ADAPTOR_ROUTE_ID)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .blockingHandler(routingContext -> {
              getAdaptorHandler(routingContext);
            });

    /*Get all adaptor status*/
    router.get(ADAPTOR_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getAdaptorHandler(routingContext);
            });

    /*Get all rules status*/
    router.get(RULE_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              getRulesHandler(routingContext);
            });

    /*Delete a adaptor*/
    router.delete(ADAPTOR_ROUTE_ID)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              deleteAdaptorHandler(routingContext);
            });

    /* Register a User (Admin) */
    router.post(USER_ROUTE)
            .consumes(MIME_APPLICATION_JSON)
            .produces(MIME_APPLICATION_JSON)
            .handler(AdminAuthHandler.create(authCred))
            .handler(routingContext -> {
              createAdaptorUser(routingContext);
            });

    /* Update a existing User (Admin) */
    router.put(USER_ROUTE)
            .consumes(MIME_APPLICATION_JSON)
            .produces(MIME_APPLICATION_JSON)
            .handler(AdminAuthHandler.create(authCred))
            .handler(routingContext -> {
              createAdaptorUser(routingContext);
            });

    /* Get all registered User (Admin) */
    router.get(USER_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AdminAuthHandler.create(authCred))
            .handler(routingContext -> {
              getAdaptorUser(routingContext);
            });

    /* Get a registered User (Admin) */
    router.get(USER_ROUTE_ID)
            .produces(MIME_APPLICATION_JSON)
            .handler(AdminAuthHandler.create(authCred))
            .handler(routingContext -> {
              getAdaptorUser(routingContext);
            });



    /* Spec test routes */

    router.post(INPUT_SPEC_ROUTE)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              HttpServerResponse response = routingContext.response();
              JsonObject jsonBody = routingContext.getBodyAsJson();
              String resp = ise.run(jsonBody.getJsonObject("inputSpec"));
              JsonObject r = new JsonObject().put("result", resp)
                      .put("success", true)
                      .put("message", "Executed successfully");
              response.setStatusCode(200)
                      .end(r.toString());
            });

    router.post(PARSE_SPEC_ROUTE)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              HttpServerResponse response = routingContext.response();
              JsonObject jsonBody = routingContext.getBodyAsJson();
              LOGGER.debug(jsonBody);
              JsonObject parsespec = jsonBody.getJsonObject("parseSpec");
              String inputData = jsonBody.getString("inputData");
              String resp = pse.run(inputData, parsespec);
              JsonObject r = new JsonObject().put("result", resp)
                      .put("success", true)
                      .put("message", "Executed successfully");
              response.setStatusCode(200)
                      .end(r.toString());
            });

    router.post(TRANSFORM_SPEC_ROUTE)
            .handler(AuthHandler.create(databaseService))
            .handler(routingContext -> {
              HttpServerResponse response = routingContext.response();
              JsonObject jsonBody = routingContext.getBodyAsJson();
              LOGGER.debug(jsonBody);
              JsonObject transformSpec = jsonBody.getJsonObject("transformSpec");
              String inputData = jsonBody.getString("inputData");
              TransformSpecEndpoint tse = new TransformSpecEndpoint(transformSpec);
              String resp = tse.run(inputData);
              JsonObject r = new JsonObject().put("success", true)
                      .put("message", "Successfully executed transform spec")
                      .put("result", resp);
              response.setStatusCode(200)
                      .end(r.toString());
            });

    router.get(ADAPTOR_RULE_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(this::getRulesByAdaptorHandler);

    router.delete(ADAPTOR_RULE_DELETE_ROUTE)
            .produces(MIME_APPLICATION_JSON)
            .handler(AuthHandler.create(databaseService))
            .handler(this::deleteRuleHandler);

    /* Start server */
    server.requestHandler(router).listen(port);

    /* Initialize support services */
    flinkClient = FlinkClientService.createProxy(vertx, FLINK_SERVICE_ADDRESS, EVENT_BUS_TIMEOUT);
    codegenInit = CodegenInitService.createProxy(vertx, CODEGENINIT_SERVICE_ADDRESS,
            EVENT_BUS_TIMEOUT);
    validator = new Validator("/jobSchema.json");
    jobScheduler = new JobScheduler(flinkClient, databaseService, quartzPropertiesPath);
    dbFlinkSync = new DbFlinkSync(flinkClient, databaseService);
    dbFlinkSync.periodicTaskScheduler();
    LOGGER.debug("Server Initialized");
  }

  /**
   * Submit Flink application Jar.
   *
   * @param routingContext
   */
  private void submitJarHandler(RoutingContext routingContext) {

    LOGGER.debug("Info: Submitting jar to Flink cluster");

    Set<FileUpload> uploads = routingContext.fileUploads();
    HttpServerResponse response = routingContext.response();
    JsonObject request = new JsonObject();

    if (uploads.isEmpty()) {
      routingContext.response().end("empty");
    } else {
      uploads.forEach(file -> {
        request.put(NAME, file.fileName())
                .put(PATH, file.uploadedFileName())
                .put(URI, JAR_UPLOAD_API);

        flinkClient.submitJar(request, responseHandler -> {
          if (responseHandler.succeeded()) {
            LOGGER.info("Info: Jar submitted successfully");
            response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .end(responseHandler.result().toString());
          } else {
            LOGGER.error("Error: Jar submission failed; " + responseHandler.cause().getMessage());
            response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .setStatusCode(400)
                    .end(responseHandler.cause().getMessage());
          }
        });
      });
    }
  }

  /**
   * Get the details related to submitted Jar(s).
   *
   * @param routingContext
   */
  private void getJarsHandler(RoutingContext routingContext) {

    LOGGER.debug("Info: Getting Jar details from Flink cluster");

    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject();
    String jarId = routingContext.pathParam(ID);

    databaseService.registerUser(requestBody, handler -> {
    });

    if (jarId != null && jarId.endsWith(".jar")) {
      requestBody.put(URI, JAR_PLAN_API.replace("$1", jarId));
      requestBody.put(ID, jarId);
    } else {
      requestBody.put(URI, JARS);
      requestBody.put(ID, "");
    }

    flinkClient.getJarDetails(requestBody, responseHandler -> {
      if (responseHandler.succeeded()) {
        LOGGER.info("Success: search query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(responseHandler.result().toString());
      } else if (responseHandler.failed()) {
        LOGGER.error(
                "Error: Error in getting jar details; " + responseHandler.cause().getMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(responseHandler.cause().getMessage());
      }
    });
  }

  /**
   * Delete the submitted jar(s) from Flink cluster.
   *
   * @param routingContext
   */
  private void deleteJarsHandler(RoutingContext routingContext) {

    LOGGER.debug("Info: Deleting Jar details from Flink cluster");

    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject();
    String jarId = routingContext.pathParam(ID);

    if (jarId != null && jarId.endsWith(".jar")) {
      requestBody.put(ID, jarId);
      requestBody.put(URI, JARS + "/" + jarId);
    } else {
      requestBody.put(ID, "");
      requestBody.put(URI, JARS);
    }

    flinkClient.deleteItems(requestBody, responseHandler -> {
      if (responseHandler.succeeded()) {
        LOGGER.info("Success: delete query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(responseHandler.result().toString());
      } else if (responseHandler.failed()) {
        LOGGER.error("Error: Error in deleting jar items; " + responseHandler.cause().getMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(responseHandler.cause().getMessage());
      }
    });
  }

  /**
   * Run the already submitted Jar.
   *
   * @param routingContext
   */
  public void runJobHandler(RoutingContext routingContext) {

    LOGGER.debug("Info: Starting a Job");

    HttpServerResponse response = routingContext.response();
    JsonObject payloadBody = routingContext.getBodyAsJson();
    JsonObject requestBody = new JsonObject();
    String jarId = routingContext.pathParam(ID);
    String mode = routingContext.queryParams().get(MODE);


    if (jarId != null) {
      Boolean isValid = validator.validate(payloadBody.encode());
      if (isValid == Boolean.TRUE) {
        LOGGER.debug("Success: schema validated");

        if (MODES.contains(mode)) {
          if (mode.equals(START)) {
            requestBody.put(URI, JOB_SUBMIT_API.replace("$1", jarId));
          } else if (mode.equals(STOP)) {
            requestBody.put(URI, JOBS_API + jarId + SAVEPOINT);
          }

          requestBody.put(DATA, payloadBody);
          requestBody.put(ID, jarId);
          requestBody.put(MODE, mode);

          flinkClient.handleJob(requestBody, resHandler -> {
            if (resHandler.succeeded()) {
              LOGGER.info("Success: Job submitted");
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .end(resHandler.result().toString());
            } else {
              LOGGER.error("Error: Jar submission failed; " + resHandler.cause().getMessage());
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .setStatusCode(400)
                      .end(resHandler.cause().getMessage());
            }
          });
        } else {
          LOGGER.error("Error: Invalid request mode");
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                  .setStatusCode(400)
                  .end("Invalid request mode");
        }
      } else {
        LOGGER.error("Error: Schema validation failed");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end("Schema validation failed");
      }
    }
  }

  /**
   * Get the details related to Job(s).
   *
   * @param routingContext
   */
  private void getJobsHandler(RoutingContext routingContext) {

    LOGGER.debug("Info: Getting job details from Flink cluster");

    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject();
    String jobId = routingContext.pathParam(ID);

    if (jobId != null && !jobId.isEmpty()) {
      requestBody.put(URI, JOBS_API + jobId);
      requestBody.put(ID, jobId);
    } else {
      requestBody.put(URI, JOBS_API);
      requestBody.put(ID, "");
    }

    flinkClient.getJobDetails(requestBody, responseHandler -> {
      if (responseHandler.succeeded()) {
        LOGGER.info("Success: search query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(responseHandler.result().toString());
      } else if (responseHandler.failed()) {
        LOGGER.error("Error: Error in getting job details; " +
                responseHandler.cause().getMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(responseHandler.cause().getMessage());
      }
    });
  }

  /**
   * Get the log(s) files.
   *
   * @param routingContext
   */
  private void getLogsHandler(RoutingContext routingContext) {

    LOGGER.debug("Info: Getting log details from Flink cluster");

    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject();
    String logId = routingContext.pathParam(L_ID);
    String taskManagerId = routingContext.pathParam(TM_ID);

    if (logId != null) {
      requestBody.put(URI, TASKMANAGER_LOGS_API.replace("$1", taskManagerId) + logId);
      requestBody.put(ID, logId);
    } else {
      requestBody.put(URI, TASKMANAGER_API);
      requestBody.put(ID, "");
    }

    flinkClient.getLogFiles(requestBody, responseHandler -> {
      if (responseHandler.succeeded()) {
        LOGGER.info("Success: search query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(responseHandler.result().toString());
      } else if (responseHandler.failed()) {
        LOGGER.error(
                "Error: Error in getting log details; " + responseHandler.cause().getMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(responseHandler.cause().getMessage());
      }
    });
  }

  /**
   * @param routingContext
   */
  private void scheduledJobs(RoutingContext routingContext) {

    LOGGER.debug("Info: Processing config file");

    HttpServerResponse response = routingContext.response();
    JsonObject payloadBody = routingContext.getBodyAsJson();
    JsonObject requestBody = new JsonObject();
    String jarId = payloadBody.getString(ID);


    if (jarId != null) {
      requestBody.put(URI, JOB_SUBMIT_API.replace("$1", jarId));
      requestBody.put(ID, jarId);
      requestBody.put(DATA, payloadBody.getJsonObject("flinkJobArgs"));
      requestBody.put(MODE, START);
      requestBody.put(SCHEDULE_PATTERN, payloadBody.getString(SCHEDULE_PATTERN));

      jobScheduler.schedule(requestBody, resHandler -> {
        if (resHandler.succeeded()) {
          LOGGER.info("Success: Job submitted");
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                  .end(resHandler.result().toString());
        } else {
          LOGGER.error("Error: Jar submission failed; " + resHandler.cause().getMessage());
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON).setStatusCode(400)
                  .end(resHandler.cause().getMessage());
        }
      });
    }
  }

  /**
   * @param routingContext
   */
  private void getAllScheduledJobs(RoutingContext routingContext) {

    LOGGER.debug("Info: Processing config file");

    HttpServerResponse response = routingContext.response();
    //JsonObject requestBody = new JsonObject();
    //JsonObject httpPost = routingContext.getBodyAsJson();

    jobScheduler.getAllJobs(handler -> {
      if (handler.succeeded()) {
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(handler.result().encode());
      } else {
        response.end("failed");
      }
    });
  }

  /**
   * Delete the identified Job from the Scheduler.
   *
   * @param routingContext
   */
  private void deleteScheduledJobs(RoutingContext routingContext) {

    LOGGER.debug("Info: Deleting scheduled quartz job");

    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject();
    String id = routingContext.pathParam(ID);

    if (id != null) {
      requestBody.put(ADAPTOR_ID, id);
    }

    jobScheduler.deleteJobs(requestBody, responseHandler -> {
      if (responseHandler.succeeded()) {
        LOGGER.info("Success: scheduler delete query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(responseHandler.result().toString());
      } else if (responseHandler.failed()) {
        LOGGER.error("Error: Error in deleting scheduler jobs; " + responseHandler.cause());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(responseHandler.cause().getMessage());
      }
    });
  }


  /**
   * Accepts the JsonConfig, processes, creates and submit Jar.
   *
   * @param routingContext
   */
  private void createAdaptorHandler(RoutingContext routingContext) {

    LOGGER.debug("Info: Processing config file");

    final String ruleSourceExchange;
    final String ruleSourceQueue;
    final String ruleSourceRoutingKey;

    HttpServerResponse response = routingContext.response();
    String username = routingContext.request().getHeader(USERNAME);
    JsonObject jsonBody = routingContext.getBodyAsJson();
    String fileName = jsonBody.getString(NAME);
    String filePath = jarOutPath + "/" + fileName;
    String adaptorType = jsonBody.getString(ADAPTOR_TYPE, ADAPTOR_DEFAULT);
    JsonObject request = new JsonObject();
    String adaptorId = username + "_" + fileName;

    if(adaptorType.equals(ADAPTOR_ETL)) {
        try {
            onboardingValidator = new Validator("onboardingETLSchema.json");
        } catch (Exception e) {
            LOGGER.error("Error: ETL Onboarding Schema file exception");
            response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .setStatusCode(400)
                    .end(new JsonObject().put(STATUS, DUPLICATE_ADAPTOR).toString());
        }

        if(!onboardingValidator.validate(jsonBody.encode())) {
            LOGGER.error("Error: Invalid Onboarding ETL Schema");
            response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .setStatusCode(400)
                    .end(new JsonObject().put(STATUS, DUPLICATE_ADAPTOR).toString());
            return;
        } else {
            LOGGER.debug("Info: Valid Onboarding ETL Schema");
        }
    } else if(adaptorType.equals(ADAPTOR_RULE)) {
      try {
          onboardingValidator = new Validator("onboardingRulesSchema.json");
      } catch (Exception e) {
            LOGGER.error("Error: Rules Onboarding Schema file exception");
            response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .setStatusCode(400)
                    .end(new JsonObject().put(STATUS, DUPLICATE_ADAPTOR).toString());
      }

      if(!onboardingValidator.validate(jsonBody.encode())) {
            LOGGER.error("Error: Invalid Onboarding ETL Schema");
            response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .setStatusCode(400)
                    .end(new JsonObject().put(STATUS, DUPLICATE_ADAPTOR).toString());
            return;
      } else {
        LOGGER.debug("Info: Valid Onboarding Rules Schema");
      }
    }

    FileSystem fileSystem = vertx.fileSystem();
    request.put(ADAPTOR_ID, adaptorId).put(USERNAME, username)
            .put(ADAPTOR_TYPE, adaptorType);


    if (!jsonBody.containsKey("ruleSourceSpec")
            && jsonBody.getString(ADAPTOR_TYPE).equals(ADAPTOR_RULE)) {
      ruleSourceExchange = adaptorId + "_exchange";
      ruleSourceQueue = adaptorId + "_queue";
      ruleSourceRoutingKey = "rules";

      JsonObject ruleSourceSpec = new JsonObject().put("type", "rmq")
              .put("uri", "amqp://" + rmqUName + ":"
                      + rmqPassword + "@" + rmqHost
                      + ":" + rmqPort + "/" + rmqVHost)
              .put("queueName", ruleSourceQueue);
      jsonBody.put("ruleSourceSpec", ruleSourceSpec);
    } else {
      ruleSourceExchange = "";
      ruleSourceQueue = "";
      ruleSourceRoutingKey = "";
    }

    Buffer buffBody = Buffer.buffer(jsonBody.toString());

    databaseService.getAdaptor(request, getHandler -> {
      if (getHandler.succeeded()) {
        JsonArray results = getHandler.result().getJsonArray(ADAPTORS);
        LOGGER.debug("Got results from db");
        if (results.isEmpty()) {
          LOGGER.debug("No such adaptor exists, creating adaptor");
          fileSystem.writeFile(filePath, buffBody, fileHandler -> {
            if (fileHandler.succeeded()) {
              String path = FileSystems.getDefault()
                      .getPath(filePath)
                      .normalize()
                      .toAbsolutePath()
                      .toString();

              request.put(DATA, jsonBody)
                      .put(NAME, fileName + ".jar")
                      .put(PATH, path)
                      .put(URI, JAR_UPLOAD_API)
                      .put(ADAPTOR_TYPE, adaptorType)
                      .put(SCHEDULE_PATTERN, jsonBody.getString(SCHEDULE_PATTERN));


              LOGGER.debug("File read complete");

              if (adaptorType.equals(ADAPTOR_RULE)) {
                if (!ruleSourceQueue.equals("")) {
                  request.put(SOURCE_ID, jsonBody.getJsonObject(INPUT_SPEC).getString(SOURCE_ID));
                  Future<JsonObject> futExch = rmqClient.createExchange(
                          new JsonObject()
                                  .put("exchangeName", ruleSourceExchange),
                          rmqVHost);

                  futExch.compose(resExch -> {
                    Future<JsonObject> futQueue = rmqClient.createQueue(
                            new JsonObject()
                                    .put("queueName", ruleSourceQueue),
                            rmqVHost);
                    return futQueue;
                  }).compose(resQueue -> {

                    Future<JsonObject> futBind = rmqClient.bindQueue(
                            new JsonObject()
                                    .put("exchangeName", ruleSourceExchange)
                                    .put("queueName", ruleSourceQueue)
                                    .put("entities",
                                            new JsonArray()
                                                    .add(ruleSourceRoutingKey)),
                            rmqVHost);
                    return futBind;

                  }).onSuccess(sucHandler -> {
                    LOGGER.debug("Created RMQ Stuff");

                    LOGGER.debug(request.getJsonObject(DATA).toString());
                    codegenInit.mvnPkg(request, mvnHandler -> {
                      if (mvnHandler.succeeded()) {
                        response.setStatusCode(202)
                                .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                                .end(new JsonObject().put(ID, adaptorId)
                                        .put(NAME, fileName)
                                        .put(STATUS, COMPILING)
                                        .toString());
                        return;
                      }
                    });
                  });
                } else {
                  codegenInit.mvnPkg(request, mvnHandler -> {
                    if (mvnHandler.succeeded()) {
                      response.setStatusCode(202)
                              .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                              .end(new JsonObject().put(ID, adaptorId)
                                      .put(NAME, fileName)
                                      .put(STATUS, COMPILING)
                                      .toString());
                      return;
                    }
                  });
                }

              } else if (adaptorType.equals(ADAPTOR_ETL)) {
                codegenInit.mvnPkg(request, mvnHandler -> {
                  if (mvnHandler.succeeded()) {
                    response.setStatusCode(202)
                            .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                            .end(new JsonObject().put(ID, adaptorId)
                                    .put(NAME, fileName)
                                    .put(STATUS, COMPILING)
                                    .toString());
                    return;
                  }
                });
              }
            } else if (fileHandler.failed()) {
              LOGGER.error("Error: Adaptor config failure: " + fileHandler.cause().getMessage());
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .setStatusCode(400)
                      .end(new ResponseHandler.Builder()
                              .withStatus(FAILED)
                              .build().toJsonString());
            }
          });
        } else {
          LOGGER.error("Error: Duplicate adaptor config");
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                  .setStatusCode(400)
                  .end(new JsonObject().put(STATUS, DUPLICATE_ADAPTOR).toString());
        }
      }
    });
  }


  private void newRuleHander(RoutingContext routingContext) {

    LOGGER.debug("Adding new rule");
    HttpServerResponse response = routingContext.response();
    String username = routingContext.request().getHeader(USERNAME);
    JsonObject req = routingContext.getBodyAsJson();

    String adaptorId = req.getString(ADAPTOR_ID);
    String sqlQuery = req.getString(SQL_QUERY);
    String ruleType = req.getString(RULE_TYPE);
    final String ruleName = username + "_" + req.getString(RULE_NAME);
    int windowMinutes = req.getInteger(WINDOW_MINUTES);


    LOGGER.debug("Processing request");
    JsonObject request = new JsonObject().put(USERNAME, username)
            .put(ADAPTOR_ID, adaptorId);
    LOGGER.debug(request.toString());

    databaseService.getRuleSource(request, getHandler -> {
      if (getHandler.succeeded()) {
        JsonObject resp = getHandler.result().getJsonArray("adaptors").getJsonObject(0);
        String ruleExchangeName = resp.getString("exchangeName");
        String ruleQueueName = resp.getString("queueName");

        if (ruleType.equals("RULE")) {
          String ruleOutputExchangeName = ruleName + "_output_exchange";
          String ruleOutputQueueName = ruleName + "_output_queue";
          // TODO: Something better
          String ruleOutputRoutingKey = "results";
          Future<JsonObject> futExch = rmqClient.createExchange(
                  new JsonObject()
                          .put("exchangeName", ruleOutputExchangeName),
                  rmqVHost);
          futExch.compose(resExch -> {
            Future<JsonObject> futQueue = rmqClient.createQueue(
                    new JsonObject()
                            .put("queueName", ruleOutputQueueName),
                    rmqVHost);
            return futQueue;
          }).compose(resQueue -> {
            Future<JsonObject> futBind = rmqClient.bindQueue(
                    new JsonObject()
                            .put("exchangeName", ruleOutputExchangeName)
                            .put("queueName", ruleOutputQueueName)
                            .put("entities",
                                    new JsonArray()
                                            .add(ruleOutputRoutingKey)),
                    rmqVHost);
            return futBind;

          }).onSuccess(rmqSuccessHandler -> {
            req.put("exchangeName", ruleOutputExchangeName);
            req.put("queueName", ruleOutputQueueName);
            req.put("routingKey", ruleOutputRoutingKey);
            req.put("username", username);
            req.put("ruleType", ruleType);
            req.put("ruleName", ruleName);
            databaseService.createRule(req, crHandler -> {
              Rule rule = new Rule(crHandler.result().getInteger("uuid"),
                      sqlQuery, RuleType.RULE, windowMinutes,
                      ruleOutputExchangeName, ruleOutputRoutingKey);

              Future<Void> fut = rmqClient.client.basicPublish(
                      ruleExchangeName, "rules", Buffer.buffer(rule.toString()));
              fut.onComplete(res2 -> {
                response.setStatusCode(202)
                        .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                        .end(new JsonObject().put(ID, adaptorId)
                                .put(STATUS, SUCCESS)
                                .toString());
                return;

              });
            });
          });
        }
      } else {
        LOGGER.error("Error: Job starting failed; " + getHandler.cause().getMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(getHandler.cause().getMessage());
      }
    });
  }

  /**
   * Handles the starting of the Job.
   *
   * @param routingContext
   */
  private void runAdaptorHandler(RoutingContext routingContext) {
    LOGGER.debug("Info: Handling Job operations; start");

    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject();

    String username = routingContext.request().getHeader(USERNAME);
    String adaptorId = routingContext.pathParam(ID);
    requestBody.put(USERNAME, username)
            .put(ADAPTOR_ID, adaptorId);

    databaseService.getAdaptor(requestBody, databaseHandler -> {
      if (databaseHandler.succeeded()) {

        JsonArray results = databaseHandler.result().getJsonArray(ADAPTORS);
        if (!results.isEmpty()) {
          JsonObject adaptorDetails = results.getJsonObject(0);
          String jarId = adaptorDetails.getString(JAR_ID);
          String jobId = adaptorDetails.getString(JOB_ID);
          String status = adaptorDetails.getString(STATUS, "");

          requestBody.put(DATA, new JsonObject());
          requestBody.put(MODE, START);

          if (jarId != null && !jarId.isBlank()) {
            if (status == null || !status.equalsIgnoreCase(RUNNING)) {

              requestBody.put(URI, JOB_SUBMIT_API.replace("$1", jarId));
              String schedulePattern = adaptorDetails.getString(SCHEDULE_PATTERN);

              if (adaptorDetails.containsKey(SCHEDULE_PATTERN) && schedulePattern != null) {

                requestBody.put(SCHEDULE_PATTERN, schedulePattern);
                jobScheduler.schedule(requestBody, resHandler -> {
                  if (resHandler.succeeded()) {
                    LOGGER.info("Success: Job submitted");
                    response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                            .end(resHandler.result().toString());
                  } else {
                    LOGGER.error(
                            "Error: Job schedulling failed; " + resHandler.cause().getMessage());
                    response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                            .setStatusCode(400)
                            .end(resHandler.cause().getMessage());
                  }
                });
              } else {
                flinkClient.handleJob(requestBody, resHandler -> {
                  if (resHandler.succeeded()) {
                    LOGGER.info("Success: Job submitted");

                    String newJobId = resHandler.result().getString(JOB_ID);
                    String query = INSERT_JOB.replace("$1", newJobId)
                            .replace("$2", RUNNING)
                            .replace("$3", adaptorId);

                    databaseService.updateComplex(query, updateHandler -> {
                      if (updateHandler.succeeded()) {
                        LOGGER.debug("Info: database updated");
                      } else {
                        LOGGER.error("Error: Job running; database update failed; "
                                + updateHandler.cause().getLocalizedMessage());
                      }
                    });
                    response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                            .end(new JsonObject().put(STATUS, SUCCESS).toString());
                  } else {
                    LOGGER.error("Error: Job starting failed; " + resHandler.cause().getMessage());
                    response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                            .setStatusCode(400)
                            .end(resHandler.cause().getMessage());
                  }
                });
              }
            } else {
              LOGGER.error("Error: Adaptor has running instance; JobId: " + jobId);
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .setStatusCode(400)
                      .end(new JsonObject().put(STATUS, ALREADY_RUNNING).toString());
            }
          } else {
            LOGGER.error("Error: Adaptor has no compiled jar");
            response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                    .setStatusCode(400)
                    .end(new JsonObject().put(STATUS, INCOMPLETE_CODEGEN).toString());
          }
        } else {
          LOGGER.error("Error: Adaptor not found");
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                  .setStatusCode(404)
                  .end(new JsonObject().put(STATUS, "adaptorNotFound").toString());
        }
      }
    });
  }

  /**
   * Handles stopping of the running Job.
   *
   * @param routingContext
   */
  private void stopAdaptorHandler(RoutingContext routingContext) {
    LOGGER.debug("Info: Handling Job operations; stop");

    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject();

    String username = routingContext.request().getHeader(USERNAME);
    String adaptorId = routingContext.pathParam(ID);
    requestBody.put(USERNAME, username).put(ADAPTOR_ID, adaptorId);

    databaseService.getAdaptor(requestBody, databaseHandler -> {
      if (databaseHandler.succeeded()) {

        JsonArray results = databaseHandler.result().getJsonArray(ADAPTORS);
        if (!results.isEmpty()) {
          JsonObject adaptorDetails =
                  databaseHandler.result().getJsonArray(ADAPTORS).getJsonObject(0);
          String jobId = adaptorDetails.getString(JOB_ID);
          String status = adaptorDetails.getString(STATUS, "");
          String jarId = adaptorDetails.getString(JAR_ID);

          jobScheduler.deleteJobs(requestBody, scheduleHandler -> {
            if (scheduleHandler.succeeded()) {
              LOGGER.debug("Info: Stopping job; Scheduler trigger cleared");

              if (jarId != null && !jarId.isBlank()) {
                if (status != null) {
                  requestBody.put(URI, JOBS_API + jobId);
                  requestBody.put(JOB_ID, jobId);
                  requestBody.put(DATA, new JsonObject());
                  requestBody.put(MODE, STOP);

                  flinkClient.handleJob(requestBody, resHandler -> {
                    if (resHandler.succeeded()) {
                      String flinkStatus = resHandler.result().getString(STATUS);
                      LOGGER.info("Success: Job stopped");
                      String query = UPDATE_JOB.replace("$1", jobId)
                              .replace("$2", flinkStatus);

                      databaseService.updateComplex(query, updateHandler -> {
                        if (updateHandler.succeeded()) {
                          LOGGER.debug("Info: database updated");
                          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                                  .end(new JsonObject().put(STATUS, SUCCESS).toString());

                        } else {
                          LOGGER.error("Error: Stopping job; database update failed; "
                                  + updateHandler.cause().getLocalizedMessage());
                        }
                      });
                    } else {
                      LOGGER.error("Error: Adaptor has running instance");
                      response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                              .setStatusCode(400)
                              .end(new JsonObject().put(STATUS, NO_RUNNING_INS).toString());
                    }
                  });
                } else {
                  LOGGER.error("Error: Adaptor has running instance");
                  response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                          .setStatusCode(400)
                          .end(new JsonObject().put(STATUS, NO_RUNNING_INS).toString());
                }
              } else {
                LOGGER.error("Error: Adaptor has no compiled jar");
                response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                        .setStatusCode(400)
                        .end(new JsonObject().put(STATUS, INCOMPLETE_CODEGEN).toString());
              }
            } else if (databaseHandler.failed()) {
              LOGGER.error("Error: Delete adptor query failed; " + scheduleHandler.cause());
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .setStatusCode(400)
                      .end(scheduleHandler.cause().getLocalizedMessage());
            }
          });
        } else {
          LOGGER.error("Error: Adaptor not found");
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                  .setStatusCode(404)
                  .end(new JsonObject().put(STATUS, ADAPTOR_NOT_FOUND).toString());
        }
      }
    });
  }

  /**
   * Get a or all the Adaptor and their details.
   *
   * @param routingContext
   */
  private void getAdaptorHandler(RoutingContext routingContext) {
    LOGGER.debug("Info: Getting adaptor status");

    HttpServerResponse response = routingContext.response();
    String username = routingContext.request().getHeader(USERNAME);
    String id = routingContext.pathParam(ID);
    JsonObject requestBody = new JsonObject().put(USERNAME, username).put(ADAPTOR_ID, id);

    databaseService.getAdaptor(requestBody, databaseHandler -> {
      if (databaseHandler.succeeded()) {
        LOGGER.info("Success: Get adaptor query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(databaseHandler.result().toString());
      } else if (databaseHandler.failed()) {
        LOGGER.error("Error: Get adptor query failed; " + databaseHandler.cause());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(databaseHandler.cause().getMessage());
      }
    });
  }

  private void getRulesHandler(RoutingContext routingContext) {
    LOGGER.debug("Info: Getting adaptor status");

    HttpServerResponse response = routingContext.response();
    String username = routingContext.request().getHeader(USERNAME);

    JsonObject requestBody = new JsonObject().put(USERNAME, username);

    databaseService.getRuleSources(requestBody, databaseHandler -> {
      if (databaseHandler.succeeded()) {
        LOGGER.info("Success: Get rules query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(databaseHandler.result().toString());
      } else if (databaseHandler.failed()) {
        LOGGER.error("Error: Get rules query failed; " + databaseHandler.cause());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(databaseHandler.cause().getMessage());
      }
    });
  }

  /**
   * Delete a adaptor, its job data, status, cleans the scheduler, jars etc.
   *
   * @param routingContext
   */
  private void deleteAdaptorHandler(RoutingContext routingContext) {
    LOGGER.debug("Info: Deleting a adaptor");

    HttpServerResponse response = routingContext.response();
    JsonObject requestBody = new JsonObject();
    String username = routingContext.request().getHeader(USERNAME);
    String id = routingContext.pathParam(ID);
    requestBody.put(USERNAME, username)
            .put(ADAPTOR_ID, id);

    if (id != null) {
      databaseService.deleteAdaptor(requestBody, databaseHandler -> {
        if (databaseHandler.succeeded()) {

          String jarId = databaseHandler.result().getString(JAR_ID);
          JsonObject adapterData = databaseHandler.result().getJsonObject(DATA);

          if (jarId != null) {
            requestBody.put(ID, jarId);
            requestBody.put(URI, JARS + "/" + jarId);
            flinkClient.deleteItems(requestBody, deleteHandler -> {
              if (deleteHandler.failed()) {
                LOGGER.error("Error: Delete Flink jar query failed; " + deleteHandler.cause()
                        .getLocalizedMessage());
              }
            });
          }

          if (adapterData != null) {
            JsonObject inputSpec = adapterData.getJsonObject(INPUT_SPEC);
            Boolean isBoundedJob = inputSpec.getBoolean(BOUNDED_JOB);
            JsonObject minioConfigObject = inputSpec.getJsonObject(MINIO_CONFIG);

            if (isBoundedJob != null && isBoundedJob && minioConfigObject != null) {
              MinioConfig minioConfig =
                      new MinioConfig.Builder(minioConfigObject.getString(MINIO_URL))
                              .bucket(minioConfigObject.getString(BUCKET))
                              .object(minioConfigObject.getString(STATE_NAME))
                              .credentials(minioConfigObject.getString(ACCESS_KEY),
                                      minioConfigObject.getString(SECRET_KEY))
                              .build();

              MinioClientHelper minioClientHelper = new MinioClientHelper(minioConfig);
              minioClientHelper.removeObject();
              LOGGER.debug("Info: Job State cleared");
            }
          }

          jobScheduler.deleteJobs(requestBody, scheduleHandler -> {
            if (scheduleHandler.succeeded()) {
              LOGGER.info("Success: Delete adaptor query");
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .end(new JsonObject().put(STATUS, SUCCESS).toString());

            } else if (scheduleHandler.failed()) {
              LOGGER.error("Error: Delete adptor query failed; " + scheduleHandler.cause()
                      .getLocalizedMessage());
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .setStatusCode(400)
                      .end(scheduleHandler.cause().getLocalizedMessage());
            }
          });
        } else {
          LOGGER.error("Error: Delete adptor query failed; " + databaseHandler.cause()
                  .getLocalizedMessage());
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                  .setStatusCode(400)
                  .end(databaseHandler.cause().getLocalizedMessage());
        }
      });
    } else {
      LOGGER.error("Error: Query param missing;");
      response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
              .setStatusCode(400)
              .end(new ResponseHandler.Builder().withStatus(INVALID_SYNTAX).build().toJsonString());
    }
  }

  /**
   * Create Adaptor users, Admin Requests.
   *
   * @param routingContext
   */
  private void createAdaptorUser(RoutingContext routingContext) {
    LOGGER.debug("Info: Registering adaptor users");

    HttpServerResponse response = routingContext.response();
    JsonObject jsonBody = routingContext.getBodyAsJson();
    HttpMethod methodType = routingContext.request().method();

    if (jsonBody.containsKey(USERNAME) && jsonBody.containsKey(PASSWORD)) {
      if (methodType == HttpMethod.POST) {
        jsonBody.put(MODE, methodType.toString());
      } else if (methodType == HttpMethod.PUT) {
        jsonBody.put(MODE, methodType.toString());

        if (jsonBody.containsKey(STATUS) && !ALLOWED_USER_STATUS.contains(
                jsonBody.getString(STATUS))) {
          LOGGER.error("Error: Invalid register query");
          response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                  .setStatusCode(400)
                  .end(new JsonObject().put(STATUS, INVALID_SYNTAX).toString());
        }
      } else {
        LOGGER.error("Error: Invalid register query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(new JsonObject().put(STATUS, INVALID_SYNTAX).toString());
      }
    } else if (methodType == HttpMethod.PUT && jsonBody.containsKey(USERNAME)
            && ALLOWED_USER_STATUS.contains(jsonBody.getString(STATUS))) {
      jsonBody.put(MODE, STATUS);
    } else {
      LOGGER.error("Error: Invalid register query");
      response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
              .setStatusCode(400)
              .end(new JsonObject().put(STATUS, INVALID_SYNTAX).toString());
    }

    databaseService.registerUser(jsonBody, databaseHandler -> {
      if (databaseHandler.succeeded()) {
        LOGGER.info("Success: Register user query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(databaseHandler.result().toString());
      } else if (databaseHandler.failed()) {
        LOGGER.error("Error: Register user query failed; " + databaseHandler.cause()
                .getLocalizedMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(databaseHandler.cause().getLocalizedMessage());
      }
    });
  }

  /**
   * Get registered users, Admin Requests.
   *
   * @param routingContext
   */
  private void getAdaptorUser(RoutingContext routingContext) {
    LOGGER.debug("Info: Getting registered users");

    HttpServerResponse response = routingContext.response();
    String id = routingContext.pathParam(ID);
    JsonObject requestBody = new JsonObject().put(ID, id);

    databaseService.getAdaptorUser(requestBody, databaseHandler -> {
      if (databaseHandler.succeeded()) {
        LOGGER.info("Success: Get user query");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(databaseHandler.result().toString());
      } else if (databaseHandler.failed()) {
        LOGGER.error(
                "Error: Get user query failed; " + databaseHandler.cause().getLocalizedMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(databaseHandler.cause().getLocalizedMessage());
      }
    });
  }


  private void getRulesByAdaptorHandler(RoutingContext routingContext) {
    LOGGER.debug("Info: Getting rules for the adaptor");

    HttpServerResponse response = routingContext.response();
    String adaptorId = routingContext.pathParam(ID);
    String username = routingContext.request().getHeader(USERNAME);

    JsonObject requestBody = new JsonObject().put(USERNAME, username).put(ADAPTOR_ID, adaptorId);

    databaseService.getRulesByAdaptor(requestBody, databaseHandler -> {
      if (databaseHandler.succeeded()) {
        LOGGER.info("Success: Get rules query by adaptor id");
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .end(databaseHandler.result().toString());
      } else if (databaseHandler.failed()) {
        LOGGER.error("Error: Get rules query by adaptor id failed; " + databaseHandler.cause());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(databaseHandler.cause().getMessage());
      }
    });
  }


  private void deleteRuleHandler(RoutingContext routingContext) {
    LOGGER.debug("Deleting rule");
    HttpServerResponse response = routingContext.response();
    String username = routingContext.request().getHeader(USERNAME);

    String adaptorId = routingContext.pathParam(ADAPTOR_ID);
    String ruleId = routingContext.pathParam(RULE_ID);

    JsonObject request = new JsonObject().put(USERNAME, username)
            .put(ADAPTOR_ID, adaptorId);

    LOGGER.debug(request.toString());

    databaseService.getRuleSource(request, getHandler -> {
      if (getHandler.succeeded()) {
        JsonObject resp = getHandler.result().getJsonArray("adaptors").getJsonObject(0);
        String ruleExchangeName = resp.getString("exchangeName");

        Rule rule = new Rule(Integer.parseInt(ruleId), RuleType.DELETE);
        Future<Void> fut = rmqClient.client.basicPublish(ruleExchangeName, "rules",
                Buffer.buffer(rule.toString()));
        fut.onComplete(res2 -> {
          JsonObject deleteRequest = new JsonObject().put(USERNAME, username)
                  .put(ADAPTOR_ID, adaptorId).put(RULE_ID, ruleId);
          databaseService.deleteRule(deleteRequest, handler -> {
            if (handler.succeeded()) {
              response.setStatusCode(202)
                      .putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .end(new JsonObject().put(RULE_ID, ruleId)
                              .put(STATUS, SUCCESS)
                              .toString());

            } else if (handler.failed()) {
              LOGGER.error("Error: Delete rule failed; " + handler.cause());
              response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                      .setStatusCode(400)
                      .end(handler.cause().getMessage());
            }
          });
        });
      } else {
        LOGGER.error("Error: Job starting failed; " + getHandler.cause().getMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(getHandler.cause().getMessage());
      }
    });
  }
}
