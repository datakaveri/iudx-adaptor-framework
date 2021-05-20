package in.org.iudx.adaptor.server.codegeninit;

import static in.org.iudx.adaptor.server.util.Constants.*;
import java.io.File;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import in.org.iudx.adaptor.server.JobScheduler;
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.file.CopyOptions;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class CodegenInitServiceImpl implements CodegenInitService {

  private static final Logger LOGGER = LogManager.getLogger(CodegenInitServiceImpl.class);
  FileSystem fileSystem;
  Vertx vertx;
  FlinkClientService flinkClient;
  DatabaseService databaseService;
  JsonObject mvnProgress = new JsonObject();
  static JobScheduler jobScheduler;

  private String templatePath;
  private String jarOutPath;

  public CodegenInitServiceImpl(Vertx vertx, FlinkClientService flinkClient, 
                                  DatabaseService databaseService, String templatePath, String jarOutPath) {
    fileSystem = vertx.fileSystem();
    this.vertx = vertx;
    this.flinkClient = flinkClient;
    this.templatePath = templatePath;
    this.jarOutPath = jarOutPath;
    this.databaseService = databaseService;
  }
  
  public static void setSchedulerInstance(JobScheduler jobScheduler1) {
    jobScheduler = jobScheduler1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CodegenInitService mvnPkg(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {

    databaseService.createAdaptor(request, adaptorHandler -> {
      if (adaptorHandler.succeeded()) {

        String path = request.getString("path");
        Future<JsonObject> mvnExecuteFuture = mvnExecute(path);

        mvnExecuteFuture.compose(mvnExecuteResponse -> {
          return submitConfigJar(request, flinkClient);

        }).compose(submitConfigJarResponse -> {
          String jarPath = submitConfigJarResponse.getString("filename");
          String jarId = jarPath.substring(jarPath.lastIndexOf("/") + 1);
          request.put(URI, JOB_SUBMIT_API.replace("$1", jarId))
          .put(ID, jarId)
          .put(DATA, new JsonObject())
          .put(MODE, START)
          .put(JAR_ID, jarId);
          return scheduleJobs(request);

        }).onComplete(composeHandler -> {
          if (composeHandler.succeeded()) {
            
            String query = UPDATE_COMPLEX
                .replace("$1", request.getString(JAR_ID))
                .replace("$2", request.getString(ADAPTOR_ID))
                .replace("$3", SCHEDULED);
            
            databaseService.updateComplex(query, updateHandler -> {
              if(updateHandler.succeeded()) {
                LOGGER.debug("Info: Job scheduled;");
              } else {
                LOGGER.error("Error: Job Scheduled; Update failed");
              }
              handler.handle(Future.succeededFuture(composeHandler.result())); 
            });
          } else if (composeHandler.failed()) {
            LOGGER.error("Error: Job scheduling failed; " + composeHandler.cause().getMessage());
            handler.handle(Future.failedFuture(composeHandler.cause().getMessage()));
          }
        });
      }
    });

    return this;
  }

  /**
   * Polling codegen progess.
   * @param handler
   * @return
   */
  @Override
  public CodegenInitService getMvnStatus(Handler<AsyncResult<JsonObject>> handler) {

    JsonObject temp = new JsonObject();
    
    if(mvnProgress.isEmpty()) {
      temp.put(STATUS, SUCCESS).put(RESULTS, new JsonArray());
    } else {
      temp.put(STATUS, SUCCESS).put(RESULTS, new JsonArray().add(mvnProgress));
    }
    handler.handle(Future.succeededFuture(temp));
    return this;
  }
  
  /**
   * 
   * @param configPath
   * @return
   */
  private Future<JsonObject> mvnExecute(String configPath) {
    
    LOGGER.debug("Info: Compiling config file; Generating flink jar");
    
    Promise<JsonObject> promise = Promise.promise();
    String fileName = new File(configPath).getName();
    CopyOptions options = new CopyOptions().setReplaceExisting(true);
    String destinationDirectory = "../" + fileName + "-" + System.currentTimeMillis();

    fileSystem.copyRecursive(templatePath, destinationDirectory, true, directoryHandler -> {
      if (directoryHandler.succeeded()) {
        LOGGER.debug("Info: Temp directory created; " + destinationDirectory);
        
        InvocationRequest mvnRequest = new DefaultInvocationRequest();
        mvnRequest.setBaseDirectory(new File(destinationDirectory));
        
        LOGGER.debug("Adaptor Config path is: "+configPath);
        mvnRequest.setGoals(Arrays.asList("-T 1","-DADAPTOR_CONFIG_PATH=" + configPath,
                                        "clean", "package",
                                        "-Dmaven.test.skip=true"));
        
        Invoker invoker = new DefaultInvoker();

        vertx.executeBlocking(blockingCodeHandler -> {
          try {
            invoker.execute(mvnRequest);
            fileSystem.move(destinationDirectory + "/target/adaptor.jar",
                             jarOutPath + "/" + fileName, options,
                mvHandler -> {
                  if (mvHandler.succeeded()) {
                    tempCleanUp(destinationDirectory);
                    blockingCodeHandler.complete(new JsonObject().put(STATUS, SUCCESS));
                  } else if (mvHandler.failed()) {
                    blockingCodeHandler.fail(new JsonObject().put(STATUS, FAILED).toString());
                  }
                });

          } catch (MavenInvocationException e) {
            LOGGER.error(e);
            blockingCodeHandler.fail(new JsonObject().put(STATUS, FAILED).toString());
          }
        },true, resultHandler -> {
          if (resultHandler.succeeded()) {
            promise.complete((JsonObject)resultHandler.result());
          } else if (resultHandler.failed()) {
            promise.fail(resultHandler.cause());
          }
        });
      } else if (directoryHandler.failed()) {
        promise.fail(new JsonObject().put(STATUS, FAILED).toString());
      }
    });
    return promise.future();
  }


  /**
   * Submit jar; To submit adaptor jar generated to flink.
   * @param request
   * @param flinkClient
   * @return promise
   */
  private Future<JsonObject> submitConfigJar(JsonObject request, FlinkClientService flinkClient) {

    Promise<JsonObject> promise = Promise.promise();

    vertx.executeBlocking(blockingCodeHandler -> {
      flinkClient.submitJar(request, responseHandler -> {
        if (responseHandler.succeeded()) {
          LOGGER.info("Info: Jar submitted successfully");
          tempCleanUp(request.getString("path"));
          blockingCodeHandler.complete(responseHandler.result());
        } else {
          LOGGER.error("Error: Jar submission failed; " + responseHandler.cause().getMessage());
          blockingCodeHandler.fail(responseHandler.cause());
        }
      });
    },true, resultHandler -> {
      if (resultHandler.succeeded()) {
        promise.complete((JsonObject) resultHandler.result());
      } else if (resultHandler.failed()) {
        promise.fail(resultHandler.cause());
      }
    });
    
    return promise.future();
  }
  
  /**
   * For scheduling jobs based on the pattern provided in the request.
   * @param request
   * @return promise
   */
  private Future<JsonObject> scheduleJobs(JsonObject request) {
    Promise<JsonObject> promise = Promise.promise();

    jobScheduler.schedule(request, resHandler -> {
      if (resHandler.succeeded()) {
        promise.complete(resHandler.result());
      } else {
        promise.fail(resHandler.cause().getMessage());
      }
    });
    return promise.future();
  }
  
  /**
   * Deleting the temp files and directories.
   * @param path
   * @return
   */
  private boolean tempCleanUp(String path) {
    
    LOGGER.debug("Info: Cleaning temp file & diretories");
    Future<Void> promise = fileSystem.deleteRecursive(path, true);
    return promise.succeeded();
  }
}
