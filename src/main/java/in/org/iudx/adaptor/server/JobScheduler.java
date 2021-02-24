package in.org.iudx.adaptor.server;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import static in.org.iudx.adaptor.server.util.Constants.*;
import java.util.Date;
import java.util.List;

public class JobScheduler {

  private Scheduler scheduler;
  private FlinkClient flinkClient;
  
  public JobScheduler(FlinkClient flinkClient, String propertiesName) throws SchedulerException {
    
    this.flinkClient = flinkClient;
    SchedulerFactory factory = new StdSchedulerFactory(propertiesName);
    scheduler = factory.getScheduler();
    scheduler.start();
  }
  
  /* Check if schedule job already exists */
  public JobScheduler checkJob(String jobId, Handler<AsyncResult<JsonObject>> handler) throws SchedulerException {
    
    if (scheduler.checkExists(new JobKey(jobId))) {
      handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
    } else {
      handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, FAILED)));
    }
    return this;
  }
    
  public JobScheduler schedule(JsonObject config, Handler<AsyncResult<JsonObject>> handler) {
    return null;

  }

  public JobScheduler schedulePlan(JsonObject config, Handler<AsyncResult<JsonObject>> handler) {
    
    return null;
  }
  
  public JobScheduler getAllJobs(Handler<AsyncResult<JsonObject>> handler) {
    try {
      for (String groupName : scheduler.getJobGroupNames()) {

        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

         String jobName = jobKey.getName();
         String jobGroup = jobKey.getGroup();

         //get job's trigger
         List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
         Date nextFireTime = triggers.get(0).getNextFireTime(); 

           System.out.println("[jobName] : " + jobName + " [groupName] : "
               + jobGroup + " - " + nextFireTime);
         }
       }
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

}
