package in.org.iudx.adaptor.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.org.iudx.adaptor.server.codegeninit.CodegenInitService;
import in.org.iudx.adaptor.server.codegeninit.CodegenInitServiceImpl;
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import in.org.iudx.adaptor.server.flink.FlinkClientServiceImpl;
import in.org.iudx.adaptor.server.util.AuthHandler;
import in.org.iudx.adaptor.server.util.Constants;
import in.org.iudx.adaptor.server.util.Validator;
import static in.org.iudx.adaptor.server.util.Constants.*;
import java.nio.file.FileSystems;
import java.util.Set;

/**
 * The Adaptor API Server API Verticle.
 *
 * <h1>Adaptor Server API Verticle</h1>
 *
 */

public class Server extends AbstractVerticle {

  private HttpServer server;
  private FlinkClientService flinkClient ;

  @SuppressWarnings("unused")
  private Router router;

  private String keystore;
  private String keystorePassword;
  private boolean isSsl;
  private int port;
  private JsonObject flinkOptions = new JsonObject();
  private Validator validator;
  private JobScheduler jobScheduler;
  private CodegenInitService codegenInit;
  private JsonObject authCred;
  private String quartzPropertiesPath;
  private String jarOutPath;
  private DatabaseService databaseService;
  private DbFlinkSync dbFlinkSync;

  private static final Logger LOGGER = LogManager.getLogger(Server.class);

  @Override
  public void start() throws Exception {
    router = Router.router(vertx);

    LOGGER.debug("config" + config());
    keystore = config().getString(KEYSTORE_PATH);
    keystorePassword = config().getString(KEYSTORE_PASSWORD);
    isSsl = config().getBoolean(IS_SSL);
    port = config().getInteger(PORT);
    flinkOptions = config().getJsonObject(FLINKOPTIONS);
    authCred = config().getJsonObject("auth");
    quartzPropertiesPath = config().getString(QUARTZ_PROPERTIES_PATH);
    jarOutPath = config().getString(JAR_OUT_PATH);

    databaseService = DatabaseService.createProxy(vertx, DATABASE_SERVICE_ADDRESS);
    HttpServerOptions serverOptions = new HttpServerOptions();

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
                   .allowedHeaders(ALLOWED_HEADERS));

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
    
    /*Config file processing*/
    router.post(CONFIG_ROUTE)
          .consumes(MIME_APPLICATION_JSON)
          .produces(MIME_APPLICATION_JSON)
          .handler(AuthHandler.create(databaseService))
          .handler(routingContext -> {
            submitConfigHandler(routingContext);
          });
    
    /*Polling config codegen*/
    router.get(CONFIG_ROUTE)
          .produces(MIME_APPLICATION_JSON)
          .handler(AuthHandler.create(databaseService))
          .blockingHandler(routingContext -> {
            statusConfigHandler(routingContext);
          });

    /* Start server */
    server.requestHandler(router).listen(port);

    /* Initialize support services */
    flinkClient = FlinkClientService.createProxy(vertx, FLINK_SERVICE_ADDRESS,EVENT_BUS_TIMEOUT);
    codegenInit = CodegenInitService.createProxy(vertx, CODEGENINIT_SERVICE_ADDRESS, EVENT_BUS_TIMEOUT);
    validator = new Validator("/jobSchema.json");
    jobScheduler = new JobScheduler(flinkClient, quartzPropertiesPath);
    dbFlinkSync = new DbFlinkSync(flinkClient,databaseService);
    dbFlinkSync.periodicTaskScheduler();
    CodegenInitServiceImpl.setSchedulerInstance(jobScheduler);
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

    databaseService.registerUser(requestBody, handler ->{});
    
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
        LOGGER.error("Error: Error in getting jar details; " + responseHandler.cause().getMessage());
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
      requestBody.put(URI, TASKMANAGER_LOGS_API.replace("$1",taskManagerId) + logId);
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
        LOGGER.error("Error: Error in getting log details; " + responseHandler.cause().getMessage());
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(400)
                .end(responseHandler.cause().getMessage());
      }
    });
  }
  
  /**
   * 
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
   * 
   * @param routingContext
   */
  private void getAllScheduledJobs(RoutingContext routingContext) {
    
    LOGGER.debug("Info: Processing config file");
    
    HttpServerResponse response = routingContext.response();
    //JsonObject requestBody = new JsonObject();
    //JsonObject httpPost = routingContext.getBodyAsJson();
    
    jobScheduler.getAllJobs(handler ->{
      if(handler.succeeded()) {
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON).end(handler.result().encode());
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
      requestBody.put(ID, id);
    } else {
      requestBody.put(ID, "");
      requestBody.put(URI, JARS);
    }

    jobScheduler.deleteJobs(requestBody, responseHandler ->{
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
   * @param routingContext
   */
  private void submitConfigHandler(RoutingContext routingContext) {

    LOGGER.debug("Info: Processing config file");

    HttpServerResponse response = routingContext.response();
    Buffer buffBody = routingContext.getBody();
    JsonObject jsonBody = routingContext.getBodyAsJson();
    String fileName = jsonBody.getString(NAME);
    String filePath = jarOutPath + "/" + fileName;
    JsonObject request = new JsonObject();

    FileSystem fileSystem = vertx.fileSystem();
    fileSystem.writeFile(filePath, buffBody, fileHandler -> {
      if (fileHandler.succeeded()) {
        String path = FileSystems.getDefault()
                                 .getPath(filePath)
                                 .normalize()
                                 .toAbsolutePath()
                                 .toString();
        
        request.put(NAME, fileName + ".jar")
               .put(PATH, path)
               .put(URI, JAR_UPLOAD_API)
               .put(SCHEDULE_PATTERN, jsonBody.getString(SCHEDULE_PATTERN));
        
        codegenInit.mvnInit(request, handler -> {
        });
        response.setStatusCode(202).end();
      } else if (fileHandler.failed()) {
        System.out.println(fileHandler.cause());
      }
    });
  }
  
  
  private void statusConfigHandler(RoutingContext routingContext) {
    LOGGER.debug("Info: Getting status of codegen");

    HttpServerResponse response = routingContext.response();
    
    codegenInit.getMvnStatus(handler->{
      if(handler.succeeded()) {
        response.putHeader(HEADER_CONTENT_TYPE, MIME_APPLICATION_JSON)
                .setStatusCode(200)
                .end(handler.result().toString());
      }
    });
  }
}
