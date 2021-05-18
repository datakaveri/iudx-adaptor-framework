package in.org.iudx.adaptor.server.util;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import in.org.iudx.adaptor.server.JobScheduler;
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static in.org.iudx.adaptor.server.util.Constants.*;

@PersistJobDataAfterExecution
public class FlinkJobExecute implements Job {
  FlinkClientService flinkClient;
  DatabaseService databaseService;
  JsonObject request = new JsonObject();

  private static final Logger LOGGER = LogManager.getLogger(FlinkJobExecute.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    
    SchedulerContext schedulerContext = null;
    flinkClient = JobScheduler.getClientInstance();
    databaseService = JobScheduler.getDbInstance();
    
    try {
      schedulerContext = context.getScheduler().getContext();
    } catch (SchedulerException e) {
      LOGGER.error("Error: Scheduler context; "+ e.getMessage());
    }
    
    //FlinkClientService flinkClient = (FlinkClientService) schedulerContext.get("flinkClient");
    //String requestBody = (String) schedulerContext.get("data");
    
    final JobDataMap  jobDataMap = context.getJobDetail().getJobDataMap();
    String requestBody = (String) jobDataMap.get("data");
    JsonObject data = new JsonObject(requestBody);
    
    flinkClient.handleJob(data, resHandler->{
      if (resHandler.succeeded()) {
        LOGGER.info("Success: Quartz job scheduled; "+ resHandler.succeeded());
        String jobId = resHandler.result()
                                 .getJsonArray(RESULTS)
                                 .getJsonObject(0)
                                 .getString(DESC);
        
        String query = INSERT_JOB.replace("$1",jobId)
                                 .replace("$2", RUNNING)
                                 .replace("$3", data.getString(ADAPTOR_ID));
        
        databaseService.updateComplex(query, updateHandler ->{
          if(updateHandler.succeeded()) {
            LOGGER.debug("Info: database updated");
          } else {
            LOGGER.error("Error: database update failed");
          }
        });
      } else {
        LOGGER.error("Error: Quartz job schedulling failed; " + resHandler.cause().getMessage());
      }
    });
  }
}
