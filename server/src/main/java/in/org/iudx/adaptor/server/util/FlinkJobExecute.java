package in.org.iudx.adaptor.server.util;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import in.org.iudx.adaptor.server.JobScheduler;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@PersistJobDataAfterExecution
public class FlinkJobExecute implements Job {
  FlinkClientService flinkClient;
  JsonObject request = new JsonObject();

  private static final Logger LOGGER = LogManager.getLogger(FlinkJobExecute.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    
    SchedulerContext schedulerContext = null;
    flinkClient = JobScheduler.getClientInstance();
    
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
      } else {
        LOGGER.error("Error: Quartz job schedulling failed; " + resHandler.cause().getMessage());
      }
    });
  }
}
