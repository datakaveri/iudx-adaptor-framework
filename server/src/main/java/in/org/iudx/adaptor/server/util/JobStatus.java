package in.org.iudx.adaptor.server.util;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import in.org.iudx.adaptor.server.JobScheduler;
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static in.org.iudx.adaptor.server.util.Constants.*;

/**
 * JobStatus - 
 * Handles the scheduling of job, interacts with FLink, and Database.
 */
@PersistJobDataAfterExecution
public class JobStatus implements Job {
  FlinkClientService flinkClient;
  DatabaseService databaseService;
  JsonObject request = new JsonObject();

  private static final Logger LOGGER = LogManager.getLogger(FlinkJobExecute.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    
    flinkClient = JobScheduler.getClientInstance();
    databaseService = JobScheduler.getDbInstance();
        
    final JobDataMap  jobDataMap = context.getJobDetail().getJobDataMap();
    String requestBody = (String) jobDataMap.get(DATA);
    JsonObject data = new JsonObject(requestBody);
    String adaptorId = data.getString(ADAPTOR_ID);
    
    String getStatus = SELECT_JOB.replace("$1", adaptorId)
                                 .replace("$2", RUNNING);
    
    databaseService.syncAdaptorJob(getStatus, jobHandler -> {
      if (jobHandler.succeeded()) {
        JsonArray jobs = jobHandler.result().getJsonArray(JOBS);
        if (jobs.size() == 0) {
          flinkClient.handleJob(data, resHandler -> {
            if (resHandler.succeeded()) {
              LOGGER.info("Success: Quartz job scheduled; " + resHandler.succeeded());
              String jobId = resHandler.result().getString(JOB_ID);
              
              String query = INSERT_JOB.replace("$1",jobId)
                                       .replace("$2", RUNNING)
                                       .replace("$3", adaptorId);
              
              databaseService.updateComplex(query, updateHandler -> {
                if (updateHandler.succeeded()) {
                  LOGGER.debug("Info: database updated");
                } else {
                  LOGGER.error("Error: database update failed");
                }
              });
            } else {
              LOGGER.error(
                  "Error: Quartz job schedulling failed; " + resHandler.cause().getMessage());
            }
          });
        } else {
          LOGGER
              .warn("Info: Cancelling the trigger; Adaptor already running: " + jobs.getString(0));
        }
      }
    });
  }
}
