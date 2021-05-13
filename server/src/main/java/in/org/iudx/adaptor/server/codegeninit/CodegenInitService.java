package in.org.iudx.adaptor.server.codegeninit;

import in.org.iudx.adaptor.server.JobScheduler;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

@VertxGen
@ProxyGen
public interface CodegenInitService {
  
  /**
   * 
   * @param jobScheduler
   * @param request
   * @param handler
   * @return
   */
  @Fluent
  CodegenInitService mvnInit(JsonObject request, Handler<AsyncResult<JsonObject>> handler);
  
  /**
   * Return codegen progess.
   * @param handler
   * @return
   */
  @Fluent
  CodegenInitService getMvnStatus(Handler<AsyncResult<JsonObject>> handler);

  /**
   * 
   * @param vertx
   * @param address
   * @return
   */
  @GenIgnore
  static CodegenInitService createProxy(Vertx vertx,  String address, Long timeout) {
    return new CodegenInitServiceVertxEBProxy(vertx, address, new DeliveryOptions().setSendTimeout(timeout));
  }
}
