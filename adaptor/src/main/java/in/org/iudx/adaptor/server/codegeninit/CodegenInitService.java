package in.org.iudx.adaptor.server.codegeninit;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface CodegenInitService {
  
  /**
   * 
   * @param path
   * @param handler
   * @return
   */
  @Fluent
  CodegenInitService mvnInit(String path, Handler<AsyncResult<JsonObject>> handler);
  
  /**
   * 
   * @param vertx
   * @param address
   * @return
   */
  @GenIgnore
  static CodegenInitService createProxy(Vertx vertx, String address) {
    return new CodegenInitServiceVertxEBProxy(vertx, address);
  }
}
