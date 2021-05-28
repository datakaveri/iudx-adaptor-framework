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
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.file.CopyOptions;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;

/**
 * Implementation of {@link CodegenInitService}. Handles and manages the Codegen related services.
 * Creates the jar, and submit the jar to the Flink using {@link FlinkClientService}.
 * 
 */
public class CodegenInitServiceImpl implements CodegenInitService {

  private static final Logger LOGGER = LogManager.getLogger(CodegenInitServiceImpl.class);
  FileSystem fileSystem;
  Vertx vertx;
  FlinkClientService flinkClient;
  DatabaseService databaseService;
  JsonObject mvnProgress = new JsonObject();

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
 
  /**
   * {@inheritDoc}
   */
  @Override
  public CodegenInitService mvnPkg(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {

    databaseService.createAdaptor(request, adaptorHandler -> {
      if (adaptorHandler.succeeded()) {

        String path = request.getString(PATH);
        Future<JsonObject> mvnExecuteFuture = mvnExecute(path);

        mvnExecuteFuture.compose(mvnExecuteResponse -> {
          return submitConfigJar(request, flinkClient);

        }).onComplete(composeHandler -> {
          String query = null;
          if (composeHandler.succeeded()) {
            LOGGER.debug("Info: Jar submitted; adaptor created");
            String jarPath = composeHandler.result().getString("filename");
            String jarId = jarPath.substring(jarPath.lastIndexOf("/") + 1);
            query = UPDATE_COMPLEX
                .replace("$1", jarId)
                .replace("$2", request.getString(ADAPTOR_ID))
                .replace("$3", COMPLETED);
          } else if (composeHandler.failed()) {
            query = UPDATE_COMPLEX
                .replace("$1", "")
                .replace("$2", request.getString(ADAPTOR_ID))
                .replace("$3", CG_FAILED);
            
            LOGGER.error(
                "Error: Adaptor gen/submission failed; " + composeHandler.cause().getMessage());
          }

          databaseService.updateComplex(query, updateHandler -> {
            if (updateHandler.failed()) {
              LOGGER.error("Error: Db Update failed");
            }
            handler.handle(Future.succeededFuture(composeHandler.result()));
          });
        });
      }
    });

    return this;
  }
  
  /**
   * Future to handles the temp directory creation, copy/move of files and directories, execution of
   * maven commands for codegen.
   * Based on Synchronous blocking of code (Vertx ExecuteBlocking).
   * 
   * @param configPath
   * @return promise
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
                jarOutPath + "/" + fileName, options, mvHandler -> {
                  if (mvHandler.succeeded()) {
                    blockingCodeHandler.complete(new JsonObject().put(STATUS, SUCCESS));
                  } else if (mvHandler.failed()) {
                    blockingCodeHandler.fail(new JsonObject().put(STATUS, FAILED).toString());
                  }
                  tempCleanUp(destinationDirectory);
                });

          } catch (MavenInvocationException e) {
            LOGGER.error(e);
            blockingCodeHandler.fail(new JsonObject().put(STATUS, FAILED).toString());
          }
        }, true, resultHandler -> {
          if (resultHandler.succeeded()) {
            promise.complete((JsonObject) resultHandler.result());
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
   * Future to submit jar; To submit generated adaptor jar to Flink. 
   * Based on Synchronous blocking of code (Vertx ExecuteBlocking).
   * 
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
        tempCleanUp(request.getString(PATH));
      });
    }, true, resultHandler -> {
      if (resultHandler.succeeded()) {
        promise.complete((JsonObject) resultHandler.result());
      } else if (resultHandler.failed()) {
        promise.fail(resultHandler.cause());
      }
    });

    return promise.future();
  }
  
  /**
   * Handle the deletion of the temp files and directories.
   * 
   * @param path
   * @return
   */
  private boolean tempCleanUp(String path) {

    LOGGER.debug("Info: Cleaning temp file & diretories");
    Future<Void> promise = fileSystem.deleteRecursive(path, true);
    return promise.succeeded();
  }
}
