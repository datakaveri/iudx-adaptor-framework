package in.org.iudx.adaptor.server.codegeninit;

import static in.org.iudx.adaptor.server.util.Constants.CODEGENINIT_SERVICE_ADDRESS;
import static in.org.iudx.adaptor.server.util.Constants.FLINK_SERVICE_ADDRESS;
import static in.org.iudx.adaptor.server.util.Constants.JAR_OUT_PATH;;
import static in.org.iudx.adaptor.server.util.Constants.TEMPLATE_PATH;;;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class CodegenInitVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(CodegenInitVerticle.class);
  private CodegenInitService codegenInitService;
  private String templatePath;
  private String jarOutPath;
  
  @Override
  public void start() throws Exception {
    
    templatePath = config().getString(TEMPLATE_PATH);
    jarOutPath = config().getString(JAR_OUT_PATH);
    FlinkClientService flinkClient = FlinkClientService.createProxy(vertx,
                                                                    FLINK_SERVICE_ADDRESS);
    codegenInitService = new CodegenInitServiceImpl(vertx, flinkClient,
                                                    templatePath, jarOutPath);
   
    new ServiceBinder(vertx).setAddress(CODEGENINIT_SERVICE_ADDRESS)
    .register(CodegenInitService.class, codegenInitService);
    
    LOGGER.debug("CodegenInitService Initialized");
  }
  
}
