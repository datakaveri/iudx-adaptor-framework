package in.org.iudx.adaptor.server;

import java.util.Timer;
import java.util.TimerTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import in.org.iudx.adaptor.server.database.DatabaseService;
import in.org.iudx.adaptor.server.flink.FlinkClientService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import static in.org.iudx.adaptor.server.util.Constants.*;

/**
 * Synchronizes the details between Flink and PostgreSQL Database. It updates Job and others tables
 * unilaterally to database. Based on {@link Timer}.
 */
public class DbFlinkSync {

  private static final Logger LOGGER = LogManager.getLogger(DbFlinkSync.class);

  private FlinkClientService flinkClient;
  private DatabaseService databaseService;

  public DbFlinkSync(FlinkClientService flinkClient, DatabaseService databaseService) {
    this.flinkClient = flinkClient;
    this.databaseService = databaseService;
  }
  
  /**
   * The scheduler schedules periodically based on the Polling Interval.
   */
  public void periodicTaskScheduler() {

    LOGGER.info("Info: Starting background sync service");

    Timer timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {

      @Override
      public void run() {
        LOGGER.debug("Info: Synchronizing Flink and Db");
        syncJobDetails();
      }
    }, 0, POLLING_INTEVAL);
  }

  /**
   * Handles the synchronization work of flink_jobs.
   */
  public void syncJobDetails() {
    JsonObject requestBody = new JsonObject();
    requestBody.put(URI, JOBS_API).put(ID, "");
    
    databaseService.syncAdaptorJob(SELECT_ALL_JOBS, handler -> {
      if (handler.succeeded()) {
        JsonArray jobIds = handler.result().getJsonArray(JOBS);

        flinkClient.getJobDetails(requestBody, flinkHandler -> {
          if (flinkHandler.succeeded()) {
            JsonArray flinkJobs = flinkHandler.result().getJsonArray(RESULTS);

            for (Object each : flinkJobs) {
              JsonObject eachFlinkJob = (JsonObject) each;

              if (!eachFlinkJob.getString(STATUS).equalsIgnoreCase(RUNNING)
                  && jobIds.contains(eachFlinkJob.getString(ID))) {
                String query = UPDATE_JOB.replace("$1", eachFlinkJob.getString(ID))
                                         .replace("$2", eachFlinkJob.getString(STATUS).toLowerCase());

                databaseService.syncAdaptorJob(query, syncHandler -> {
                  if (syncHandler.succeeded()) {
                    LOGGER.debug("Info: Synchronization complete; Status updated");
                  } else {
                    LOGGER.error(
                        "Error: Synchronization failed; " + syncHandler.cause().getMessage());
                  }
                });
              }
            }
          } else {
            LOGGER.error("Error: Synchronization failed; " + flinkHandler.cause().getMessage());
          }
        });
      } else if (handler.failed())
        LOGGER.error("Error: Synchronization failed; " + handler.cause().getMessage());
    });
  }
}
