package in.org.iudx.adaptor.server.flink;

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
public interface FlinkClientService {

  /**
   * 
   * @param request
   * @param authenticationInfo
   * @param handler
   * @return
   */
  @Fluent
  FlinkClientService submitJar(JsonObject request, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 
   * @param request
   * @param authenticationInfo
   * @param handler
   * @return
   */
  @Fluent
  FlinkClientService handleJob(JsonObject request, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 
   * @param request
   * @param authenticationInfo
   * @param handler
   * @return
   */
  @Fluent
  FlinkClientService getJarDetails(JsonObject request, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 
   * @param request
   * @param authenticationInfo
   * @param handler
   * @return
   */
  @Fluent
  FlinkClientService deleteItems(JsonObject request, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 
   * @param request
   * @param authenticationInfo
   * @param handler
   * @return
   */
  @Fluent
  FlinkClientService getJobDetails(JsonObject request, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 
   * @param request
   * @param authenticationInfo
   * @param handler
   * @return
   */
  @Fluent
  FlinkClientService getLogFiles(JsonObject request, Handler<AsyncResult<JsonObject>> handler);

  /**
   * 
   * @param vertx
   * @param address
   * @return
   */
  @GenIgnore
  static FlinkClientService createProxy(Vertx vertx, String address) {
    return new FlinkClientServiceVertxEBProxy(vertx, address);
  }

}
