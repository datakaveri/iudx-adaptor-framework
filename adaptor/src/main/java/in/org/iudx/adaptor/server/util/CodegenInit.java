package in.org.iudx.adaptor.server.util;

import static in.org.iudx.adaptor.server.util.Constants.SUCCESS;
import java.io.File;
import java.util.Arrays;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.file.CopyOptions;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import static in.org.iudx.adaptor.server.util.Constants.*;

public class CodegenInit {

  FileSystem fileSystem = Vertx.vertx().fileSystem();

  public CodegenInit mvnInit(String path, Handler<AsyncResult<JsonObject>> handler) {

    String fileName = new File(path).getName();
    InvocationRequest request = new DefaultInvocationRequest();
    request.setPomFile(new File("../pom.xml"));
    request.setGoals(Arrays.asList("-DADAPTOR_CONFIG_PATH=" + path, "clean", "package",
        "-Dmaven.test.skip=true"));

    Invoker invoker = new DefaultInvoker();

    try {
      invoker.execute(request);
      CopyOptions options = new CopyOptions().setReplaceExisting(true);
      fileSystem.move("../template/target/adaptor.jar", "../upload-jar/" + fileName, options,
          mvHandler -> {
            if (mvHandler.succeeded()) {
              handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
            } else if (mvHandler.failed()) {
              handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
            }
          });

    } catch (MavenInvocationException e) {
      e.printStackTrace();
      handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
    }
    return this;
  }
}
