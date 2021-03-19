package in.org.iudx.adaptor.server.flink;

import static in.org.iudx.adaptor.server.util.Constants.FLINKOPTIONS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import static in.org.iudx.adaptor.server.util.Constants.*;

public class FlinkClientVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(FlinkClientVerticle.class);
  private FlinkClientService flinkClientService;

  @Override
  public void start() throws Exception {
    
    JsonObject flinkOptions = config().getJsonObject(FLINKOPTIONS);
    flinkClientService = new FlinkClientServiceImpl(vertx, flinkOptions);

    new ServiceBinder(vertx).setAddress(FLINK_SERVICE_ADDRESS)
        .register(FlinkClientService.class, flinkClientService);
    
    LOGGER.debug("Flink Client Initialized");
  }

}
