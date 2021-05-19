package in.org.iudx.adaptor.server;

import static in.org.iudx.adaptor.server.util.Constants.ID;
import static in.org.iudx.adaptor.server.util.Constants.JOBS_API;
import static in.org.iudx.adaptor.server.util.Constants.URI;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.json.JsonObject;
import static in.org.iudx.adaptor.server.util.Constants.POLLING_INTEVAL;

public class DbFlinkSync {

  private static final Logger LOGGER = LogManager.getLogger(DbFlinkSync.class);
  
  private FlinkClientService flinkClient;
  private DatabaseService databaseService;

  public DbFlinkSync(FlinkClientService flinkClient, DatabaseService databaseService) {
    this.flinkClient = flinkClient;
    this.databaseService = databaseService;
  }
  
  public void periodicTaskScheduler() {
    
    LOGGER.info("Info: Starting background sync service");
    
    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      
      @Override
      public void run() {
        LOGGER.debug("Info: Synchronizing with Flink and Db");
        syncJobDetails();
      }
    }, 0, POLLING_INTEVAL);
  }
  
  public void syncJobDetails() {
    JsonObject requestBody = new JsonObject();
    requestBody.put(URI, JOBS_API).put(ID, "");
    String selectQuery = "SELECT * FROM flink_job";
    
    /*
     * databaseService.getAdaptor(selectQuery, handler ->{ if(handler.succeeded()) {
     * System.out.println(handler.result()); } else if (handler.failed())
     * System.out.println(handler.cause()); });
     */
  }
  
  public void syncJarDetails() {
    
  }
  
  public void syncOtherDetails() {
    
  }
  
  public void getDbJob() {
    
  }
}
