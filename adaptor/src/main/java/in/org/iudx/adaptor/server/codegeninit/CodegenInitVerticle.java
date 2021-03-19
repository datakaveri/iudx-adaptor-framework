package in.org.iudx.adaptor.server.codegeninit;

import static in.org.iudx.adaptor.server.util.Constants.CODEGENINIT_SERVICE_ADDRESS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ServiceBinder;

public class CodegenInitVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(CodegenInitVerticle.class);
  private CodegenInitService codegenInitService;
  
  @Override
  public void start() throws Exception {
    
    codegenInitService = new CodegenInitServiceImpl(vertx);
    
    new ServiceBinder(vertx).setAddress(CODEGENINIT_SERVICE_ADDRESS)
    .register(CodegenInitService.class, codegenInitService);
    
    LOGGER.debug("CodegenInitService Initialized");
  }
  
}
