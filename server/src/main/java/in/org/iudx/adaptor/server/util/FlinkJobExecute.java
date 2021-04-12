package in.org.iudx.adaptor.server.util;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import in.org.iudx.adaptor.server.flink.FlinkClientServiceImpl;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlinkJobExecute implements Job {
  FlinkClientServiceImpl flinkClient;
  JsonObject request = new JsonObject();

  private static final Logger LOGGER = LogManager.getLogger(FlinkJobExecute.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    
    SchedulerContext schedulerContext = null;
    try {
      schedulerContext = context.getScheduler().getContext();
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    FlinkClientServiceImpl flinkClient = (FlinkClientServiceImpl) schedulerContext.get("flinkClient");
    String requestBody = (String) schedulerContext.get("data");
    
    //final JobDataMap  jobDataMap = context.getJobDetail().getJobDataMap();
    //String requestBody = (String) jobDataMap.get("data");
    //FlinkClient flinkClient = (FlinkClient) jobDataMap.get("flinkClient");
    
    JsonObject data = new JsonObject(requestBody);
    
    flinkClient.handleJob(data, resHandler->{
      if (resHandler.succeeded()) {
        LOGGER.info("Success: Job submitted "+ resHandler.succeeded());
      } else {
        LOGGER.error("Error: Jar submission failed; " + resHandler.cause().getMessage());
      }
    });
  }
}
