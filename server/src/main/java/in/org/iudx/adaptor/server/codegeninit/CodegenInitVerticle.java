package in.org.iudx.adaptor.server.codegeninit;

import static in.org.iudx.adaptor.server.util.Constants.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class CodegenInitVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(CodegenInitVerticle.class);
  private CodegenInitService codegenInitService;
  private DatabaseService databaseService;
  private FlinkClientService flinkClient;
  private String templatePath;
  private String jarOutPath;
  
  @Override
  public void start() throws Exception {
    
    templatePath = config().getString(TEMPLATE_PATH);
    jarOutPath = config().getString(JAR_OUT_PATH);
    flinkClient = FlinkClientService.createProxy(vertx,
                                                                    FLINK_SERVICE_ADDRESS, EVENT_BUS_TIMEOUT);
    databaseService = DatabaseService.createProxy(vertx, DATABASE_SERVICE_ADDRESS);
    codegenInitService = new CodegenInitServiceImpl(vertx, flinkClient, databaseService,
                                                    templatePath, jarOutPath);
   
    new ServiceBinder(vertx).setAddress(CODEGENINIT_SERVICE_ADDRESS)
    .register(CodegenInitService.class, codegenInitService);
    
    LOGGER.debug("CodegenInitService Initialized");
  }
  
}
