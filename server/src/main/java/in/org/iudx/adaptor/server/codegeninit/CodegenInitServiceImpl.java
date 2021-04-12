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
  JsonObject mvnProgress = new JsonObject();

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

  /**
   * {@inheritDoc}
   */
  @Override
  public CodegenInitService mvnInit(JsonObject request, Handler<AsyncResult<JsonObject>> handler) {
    
    String path  = request.getString("path");
    Future<JsonObject> future = mvnExecute(path);
    future.onComplete(resHandler -> {
      if (future.succeeded()) {
        submitConfigJar(request, flinkClient).onComplete(flinkHandler -> {
          if (flinkHandler.succeeded()) {
            String id = flinkHandler.result().getString("filename");
            mvnProgress.put(new File(path).getName(), new JsonObject().put(ID, id).put(STATUS, "completed"));
          } else if (flinkHandler.failed()) {
            mvnProgress.put(new File(path).getName(), new JsonObject().put(ID, null).put(STATUS, "failed"));
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
   * @param path
   * @return
   */
  private Future<JsonObject> mvnExecute(String path) {
    Promise<JsonObject> promise = Promise.promise();

    String fileName = new File(path).getName();
    InvocationRequest request = new DefaultInvocationRequest();
    request.setBaseDirectory(new File(templatePath));
    // request.setPomFile(new File(templatePath + "/pom.xml"));
    LOGGER.debug("Path is ");
    LOGGER.debug(path);
    request.setGoals(Arrays.asList("-DADAPTOR_CONFIG_PATH=" + path,
                                    "clean", "package",
                                    "-Dmaven.test.skip=true"));
    
    mvnProgress.put(fileName, new JsonObject().put(ID, null).put(STATUS, "progress"));

    Invoker invoker = new DefaultInvoker();

    vertx.executeBlocking(blockingCodeHandler -> {
      try {
        invoker.execute(request);
        CopyOptions options = new CopyOptions().setReplaceExisting(true);
        fileSystem.move(templatePath + "/target/adaptor.jar",
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
    }, resultHandler -> {
      if (resultHandler.succeeded()) {
        promise.complete((JsonObject)resultHandler.result());
      } else if (resultHandler.failed()) {
        promise.fail(resultHandler.cause());
      }
    });
    return promise.future();
  }


  private Future<JsonObject> submitConfigJar(JsonObject request, FlinkClientService flinkClient) {
    Promise<JsonObject> promise = Promise.promise();

    flinkClient.submitJar(request, responseHandler -> {
      if (responseHandler.succeeded()) {
        LOGGER.info("Info: Jar submitted successfully");
        promise.complete(responseHandler.result());
      } else {
        LOGGER.error("Error: Jar submission failed; " + responseHandler.cause().getMessage());
        promise.fail(responseHandler.cause());
      }
    });
    return promise.future();
  }
}
