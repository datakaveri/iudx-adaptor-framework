package in.org.iudx.adaptor.server.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class Constants {

  /** Service Addresses */
  public static final String FLINK_SERVICE_ADDRESS = "iudx.adaptor.framework.flink.service";
  public static final String CODEGENINIT_SERVICE_ADDRESS =
      "iudx.adaptor.framework.codegenmvn.service";
  public static final String DATABASE_SERVICE_ADDRESS = "iudx.adaptor.framework.database.service";

  public static final Long EVENT_BUS_TIMEOUT = 120000L;
  public static final String IS_SSL = "ssl";
  public static final String PORT = "port";
  public static final String KEYSTORE_PATH = "keystorePath";
  public static final String KEYSTORE_PASSWORD = "keystorePassword";
  public static final String TEMPLATE_PATH = "templatePath";
  public static final String JAR_OUT_PATH = "jarOutPath";


  /** Accept Headers and CORS */
  public static final String HEADER_ACCEPT = "Accept";
  public static final String HEADER_TOKEN = "token";
  public static final String HEADER_CONTENT_LENGTH = "Content-Length";
  public static final String HEADER_CONTENT_TYPE = "Content-Type";
  public static final String HEADER_HOST = "Host";
  public static final String HEADER_INSTANCE = "instance";
  public static final String HEADER_ORIGIN = "Origin";
  public static final String HEADER_REFERER = "Referer";
  public static final String HEADER_CORS = "Access-Control-Allow-Origin";
  public static final Set<String> ALLOWED_HEADERS =
      new HashSet<String>(Arrays.asList(HEADER_ACCEPT, HEADER_TOKEN, HEADER_CONTENT_LENGTH,
          HEADER_CONTENT_TYPE, HEADER_HOST, HEADER_ORIGIN, HEADER_REFERER, HEADER_CORS));

  /** Mime Type */
  public static final String MIME_APPLICATION_JSON = "application/json";
  public static final String MIME_TEXT_HTML = "text/html";
  public static final String APPLICATION_X_WWW_FORM_URLENCODED =
      "application/x-www-form-urlencoded";
  public static final String MULTIPART_FORM_DATA = "multipart/form-data";
  public static final String JAVA_ARCHIVE = "application/x-java-archive";


  /** Routes */
  public static final String JARS = "/jars";
  private static String basePath = "/iudx/adaptor/v1";
  public static final String JAR_ROUTE = basePath + "/jar";
  public static final String GET_JAR_ROUTE = basePath + "/jar/:id";
  public static final String JOBS_ROUTE = basePath + "/job";
  public static final String JOB_RUN_ROUTE = basePath + "/job/:id";
  public static final String JOB_ROUTE = basePath + "/job/:id";
  public static final String LOGS_ROUTE = basePath + "/log";
  public static final String LOG_ROUTE = basePath + "/log/:tId/:lId";
  public static final String SCHEDULER_ROUTE = basePath + "/schedule";
  public static final String DELETE_SCHEDULER_JOB = basePath + "/schedule/:id";
  
  public static final String ADAPTOR_ROUTE = "/adaptor";
  public static final String ADAPTOR_ROUTE_ID = ADAPTOR_ROUTE + "/:id";

  /** Response messages */
  public static final String MESSAGE = "message";
  public static final String RESULTS = "results";
  public static final String METHOD = "method";
  public static final String STATUS = "status";
  public static final String FAILED = "failed";
  public static final String ERROR = "error";
  public static final String SUCCESS = "success";
  public static final String DELETE = "delete";
  public static final String GET = "get";
  public static final String POST = "post";
  public static final String DESC = "description";
  public static final String ID = "id";
  public static final String MODE = "mode";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";
  public static final String EXISTS = "exists";

  /** Flink URI */
  public static final String JAR_UPLOAD_API = "/jars/upload";
  public static final String CLUSTER_OVERVIEW_API = "/overview";
  public static final String CLUSTER_API = "/cluster";
  public static final String JOBS_OVERVIEW_API = "/jobs/overview";
  public static final String JAR_PLAN_API = JARS + "/$1/plan";
  public static final String JOB_SUBMIT_API = JARS + "/$1/run";
  public static final String JOBS_API = "/jobs/";
  public static final String SAVEPOINT = "/savepoints";
  public static final String TASKMANAGER_API = "/taskmanagers";
  public static final String TASKMANAGER_LOGS_API = TASKMANAGER_API + "/$1/logs/";


  /** Others */
  public static final String FLINKOPTIONS = "flinkOptions";
  public static final String QUARTZ_PROPERTIES_PATH = "quartzPropertiesPath";
  public static final String NAME = "name";
  public static final String PATH = "path";
  public static final String URI = "uri";
  public static final String FILES = "files";
  public static final String PLAN = "plan";
  public static final String DATA = "data";
  public static final String JOBS = "jobs";
  public static final String OPERATION = "operation";
  public static final String TM_ID = "tId";
  public static final String L_ID = "lId";

  /* Modes */
  public static final String START = "start";
  public static final String STOP = "stop";
  public static final String RESUME = "resume";
  public static final ArrayList<String> MODES =
      new ArrayList<String>(Arrays.asList(START, STOP, RESUME));

  public static final long POLLING_INTEVAL = 600000; // 1 Minute
  public static final String SCHEDULE_PATTERN = "schedulePattern";

  /* Database query */
  public static final String ADAPTOR_ID = "adaptorId";
  public static final String JAR_ID = "jarId";
  public static final String JOB_ID = "jobId";
  public static final String COMPILING = "compiling";
  public static final String SCHEDULED = "scheduled";
  public static final String RUNNING = "running";
  
  public static final String AUTHENTICATE_USER =
      "SELECT EXISTS ( SELECT * FROM public.\"user\" WHERE username = '$1' and password = '$2' and status = 'active')";
  public static final String CREATE_ADAPTOR =
      "INSERT into adaptor(adaptor_id,\"data\",\"timestamp\",user_id) SELECT '$1', '$2'::json, now(), us.\"id\" from public.\"user\" us where username = '$3';"
      + "INSERT into codegen_status(status, \"timestamp\", adaptor_id) values ('$4', now(),'$1')";
  
  public static final String UPDATE_STATUS = "INSERT into codegen_status(status, \"timestamp\", adaptor_id) values ('$1', now(),'$2')";
  
  public static final String UPDATE_JARID = "UPDATE adaptor SET jar_id = '$1' WHERE adaptor_id = '$2'";
  
  public static final String UPDATE_COMPLEX = "WITH update_adaptor AS (\n" + 
      "  UPDATE adaptor SET jar_id = '$1' WHERE adaptor_id = '$2'\n" + 
      "  returning adaptor_id\n" + 
      ")\n" + 
      "UPDATE codegen_status SET status = '$3', \"timestamp\" = now() \n" + 
      "  FROM (select adaptor_id from update_adaptor) AS adaptor\n" + 
      "  WHERE codegen_status.adaptor_id = adaptor.adaptor_id";
  
  public static final String INSERT_JOB = "INSERT into flink_job(job_id, \"timestamp\",status,adaptor_id) values ('$1',now(),'$2','$3')";
  
  public static final String UPDATE_COMPLEX_OLD = "WITH update_adaptor AS (\n" + 
      "  UPDATE adaptor SET jar_id = '$1' WHERE adaptor_id = '$2'\n" + 
      "  returning adaptor_id\n" + 
      "),\n" + 
      " update_codegen_status AS (\n" + 
      "  UPDATE codegen_status SET STATUS = '$3', \"timestamp\" = now() \n" + 
      "  FROM (SELECT adaptor_id FROM update_adaptor) AS adaptor\n" + 
      "  WHERE codegen_status.adaptor_id = adaptor.adaptor_id\n" + 
      "  returning adaptor.adaptor_id\n" + 
      ")\n" + 
      "INSERT INTO flink_job(job_id, \"timestamp\",status, adaptor_id)\n" + 
      "SELECT '$4',now(),'$5',adaptor_id from update_codegen_status";


}
