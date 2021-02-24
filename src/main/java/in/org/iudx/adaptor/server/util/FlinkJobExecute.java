package in.org.iudx.adaptor.server.util;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import in.org.iudx.adaptor.server.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlinkJobExecute implements  Job {
  
  private static final Logger LOGGER = LogManager.getLogger(Server.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    final JobDetail jobDetail = context.getJobDetail();
    LOGGER.info("Job {0}", jobDetail.getKey());    
  }

}
