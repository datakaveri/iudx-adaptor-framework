package in.org.iudx.adaptor.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import in.org.iudx.adaptor.server.util.FlinkJobExecute;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import static in.org.iudx.adaptor.server.util.Constants.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * Handles the scheduling of flink jobs using Quartz.
 *
 */
public class JobScheduler {

  private Scheduler scheduler;
  private FlinkClient flinkClient;
  private static final Logger LOGGER = LogManager.getLogger(JobScheduler.class);
  
  /**
   * JobScheduler constructor, initializes required fields.
   * 
   * @param flinkClient
   * @param propertiesName
   * @throws SchedulerException
   */
  public JobScheduler(FlinkClient flinkClient, String propertiesName) throws SchedulerException {
    
    this.flinkClient = flinkClient;
    SchedulerFactory factory = new StdSchedulerFactory(propertiesName);
    scheduler = factory.getScheduler();
    scheduler.start();
  }
  
  /**
   * Determine whether a Job with the given identifier already exists within the scheduler.
   * 
   * @param jobId
   * @param handler
   * @return
   * @throws SchedulerException
   */
  public JobScheduler checkJob(String jobId, Handler<AsyncResult<JsonObject>> handler) throws SchedulerException {
    
    if (scheduler.checkExists(new JobKey(jobId))) {
      handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
    } else {
      handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, FAILED)));
    }
    return this;
  }
   
  /**
   *  Add the given JobDetail to the Scheduler, and associate the given Trigger with it.
   *  
   * @param config
   * @param handler
   * @return
   */
  public JobScheduler schedule(JsonObject config, Handler<AsyncResult<JsonObject>> handler) {


    JobKey jobId = new JobKey(config.getString("id"));
    String cronExpression = config.getString("schedulePattern");
    final JobDetailImpl jobDetail = new JobDetailImpl();
    jobDetail.setKey(jobId);

    // jobDetail.getJobDataMap().put("flinkClient", this.flinkClient);
    jobDetail.getJobDataMap().put("data", config.encode());

    jobDetail.setJobClass(FlinkJobExecute.class);

    final CronTriggerImpl trigger = new CronTriggerImpl();
    trigger.setName(jobId + "Trigger");

    try {
      if (scheduler.checkExists(jobId)) {
        handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
        return this;
      } else {
        scheduler.getContext().put("flinkClient", this.flinkClient);
        scheduler.getContext().put("data",config.encode());
        trigger.setCronExpression(cronExpression);
        scheduler.scheduleJob(jobDetail, trigger);
      }
    } catch (ParseException | SchedulerException e) {
      LOGGER.error("Failed: Schedulling quartz job; " + e.getLocalizedMessage());
    }

    handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
    return this;
  }

/**
 *Get the JobDetail for the Job instance with the given key.
 * 
 * @param config
 * @param handler
 * @return
 */
  public JobScheduler schedulePlan(JsonObject config, Handler<AsyncResult<JsonObject>> handler) {
    
    return null;
  }
  
  /**
   * Get all the running job details.
   * @param handler
   * @return
   */
  public JobScheduler getAllJobs(Handler<AsyncResult<JsonObject>> handler) {
    JsonObject quartzJob = new JsonObject();
    JsonArray result = new JsonArray();
    try {
      for (String groupName : scheduler.getJobGroupNames()) {
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

         String jobName = jobKey.getName();
         String jobGroup = jobKey.getGroup();
         List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
         
         JsonObject jobTrigger = new JsonObject();
         Date nextFireTime = triggers.get(0).getNextFireTime(); 
         Date startTime = triggers.get(0).getStartTime();
         Date endTime = triggers.get(0).getEndTime(); 
         String calName = triggers.get(0).getCalendarName();
         CronTrigger cronTrigger = (CronTrigger) triggers.get(0);
         
         jobTrigger.put("startTime", startTime.toString())
                   .put("nextFireTime", nextFireTime.toString())
                   .put("endTime", endTime)
                   .put("calName", calName)
                   .put("schedulePattern", cronTrigger.getCronExpression());
                  
         quartzJob.put("jobName", jobName)
                  .put("jobGroup", jobGroup)
                  .put("trigger", new JsonArray().add(jobTrigger));

         }
        result.add(quartzJob);
       }
    } catch (SchedulerException e) {
      LOGGER.error("Failed: Getting all scheduled jobs; " + e.getLocalizedMessage());
    }
    handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS).put(RESULTS, result)));
    return this;
  }
  
  /**
   * Clears (deletes!) all scheduling data - all Jobs, Triggers Calendars 
   * or job with trigger if ID is present.
   *  
   * @param config
   * @param handler
   * @return
   */
  public JobScheduler deleteJobs(JsonObject config, Handler<AsyncResult<JsonObject>> handler) {
    
    String id = config.getString(ID, "");
    
    if(!id.isEmpty()) {
        try {
         boolean flag =  scheduler.deleteJob(new JobKey(id));
         if(flag == Boolean.FALSE) {
           handler.handle(Future.failedFuture(new JsonObject().put(STATUS, FAILED).toString()));
           return this;
         }
        } catch (SchedulerException e) {
          LOGGER.error("Failed: In scheduler job deletion; " + e.getLocalizedMessage());
        }
    } else {
        try {
          scheduler.clear();
        } catch (SchedulerException e) {
          LOGGER.error("Failed: In scheduler job deletion; " + e.getLocalizedMessage());
        }
    }
    
    handler.handle(Future.succeededFuture(new JsonObject().put(STATUS, SUCCESS)));
    return this;
  }
}
