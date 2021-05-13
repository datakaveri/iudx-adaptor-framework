package in.org.iudx.adaptor.server.codegeninit;

import static in.org.iudx.adaptor.server.util.Constants.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import in.org.iudx.adaptor.server.JobScheduler;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.netty.util.internal.StringUtil;
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
  JsonObject mvnProgress = new JsonObject();
  static JobScheduler jobScheduler;

  private String templatePath;
  private String jarOutPath;

  public CodegenInitServiceImpl(Vertx vertx, FlinkClientService flinkClient, 
                                  String templatePath, String jarOutPath) {
    fileSystem = vertx.fileSystem();
    this.vertx = vertx;
    this.flinkClient = flinkClient;
    this.templatePath = templatePath;
    this.jarOutPath = jarOutPath;
  }
  
  public static void setSchedulerInstance(JobScheduler jobScheduler1) {
    jobScheduler = jobScheduler1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CodegenInitService mvnInit(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
    
    String path  = request.getString("path");
    Future<JsonObject> mvnExecuteFuture = mvnExecute(path);
    
    mvnExecuteFuture.compose(mvnExecuteResponse -> {
      return submitConfigJar(request, flinkClient);
      
    })/*
       * .recover(mapper ->{ LOGGER.debug("Error: Recover mapper; "+ mapper.getMessage()); return
       * null;
       * 
       * })
       */.compose(submitConfigJarResponse -> {
      String jarPath = submitConfigJarResponse.getString("filename");
      String jarId = jarPath.substring(jarPath.lastIndexOf("/")+1);
      request.put(URI, JOB_SUBMIT_API.replace("$1", jarId));
      request.put(ID, jarId);
      request.put(DATA, new JsonObject());
      request.put(MODE, START);
      return scheduleJobs(request);
      
    }).onComplete(composeHandler -> {
      if(composeHandler.succeeded()) {
        LOGGER.debug("Info: Job scheduled;");
        handler.handle(Future.succeededFuture(composeHandler.result()));
      } else if(composeHandler.failed()) {
        LOGGER.error("Error: Job scheduling failed; "+ composeHandler.cause().getMessage());
        handler.handle(Future.failedFuture(composeHandler.cause().getMessage()));
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
   * @param path
   * @return
   */
  private Future<JsonObject> mvnExecute(String path) {
    
    LOGGER.debug("Info: Compiling config file; Generating flink jar");
    
    Promise<JsonObject> promise = Promise.promise();
    String fileName = new File(path).getName();
    InvocationRequest request = new DefaultInvocationRequest();
    request.setBaseDirectory(new File(templatePath));
    // request.setPomFile(new File(templatePath + "/pom.xml"));
    LOGGER.debug("Path is ");
    LOGGER.debug(path);
    request.setGoals(Arrays.asList("-T 1","-DADAPTOR_CONFIG_PATH=" + path,
                                    "clean", "package",
                                    "-Dmaven.test.skip=true"));
    
    mvnProgress.put(fileName, new JsonObject().put(ID, null).put(STATUS, "progress"));

    Invoker invoker = new DefaultInvoker();

    vertx.executeBlocking(blockingCodeHandler -> {
      try {
        invoker.execute(request);
        CopyOptions options = new CopyOptions().setReplaceExisting(true);
        fileSystem.copy(templatePath + "/target/adaptor.jar",
                         jarOutPath + "/" + fileName, options,
            mvHandler -> {
              if (mvHandler.succeeded()) {
                blockingCodeHandler.complete(new JsonObject().put(STATUS, SUCCESS));
              } else if (mvHandler.failed()) {
                blockingCodeHandler.fail(new JsonObject().put(STATUS, FAILED).toString());
              }
             // blockingCodeHandler.future();
            });

      } catch (MavenInvocationException e) {
        LOGGER.error(e);
        blockingCodeHandler.fail(new JsonObject().put(STATUS, FAILED).toString());
      }
      //blockingCodeHandler.future();
    },true, resultHandler -> {
      if (resultHandler.succeeded()) {
        promise.complete((JsonObject)resultHandler.result());
      } else if (resultHandler.failed()) {
        promise.fail(resultHandler.cause());
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
   * For schedulling jobs based on the pattern provided inthe config.
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
}
