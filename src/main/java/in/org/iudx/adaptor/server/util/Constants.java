package in.org.iudx.adaptor.server.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

public class Constants {
  public static final String IS_SSL = "ssl";
  public static final String PORT = "port";
  public static final String KEYSTORE_PATH = "keystorePath";
  public static final String KEYSTORE_PASSWORD = "keystorePassword";


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
  public static String UPLOAD_DIR = "./upload-jar";
  private static String basePath = "/iudx/adaptor/v1";
  public static final String JAR_ROUTE = basePath + "/jar";
  public static final String GET_JAR_ROUTE = basePath + "/jar/:id";
  public static final String JOBS_ROUTE = basePath + "/job";
  public static final String JOB_RUN_ROUTE = basePath + "/job/:id";
  public static final String JOB_ROUTE = basePath + "/job/:id";
  public static final String LOGS_ROUTE = basePath + "/log";
  public static final String LOG_ROUTE = basePath + "/log/:tId/:lId";

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

  /** Flink URI */
  public static final String JAR_UPLOAD_API = "/jars/upload";
  public static final String CLUSTER_OVERVIEW_API = "/overview";
  public static final String CLUSTER_API = "/cluster";
  public static final String JOBS_OVERVIEW_API = "/jobs/overview";
  public static final String JAR_PLAN_API = JARS+"/$1/plan";
  public static final String JOB_SUBMIT_API = JARS+"/$1/run";
  public static final String JOBS_API = "/jobs/";
  public static final String SAVEPOINT = "/savepoints";
  public static final String TASKMANAGER_API ="/taskmanagers";
  public static final String TASKMANAGER_LOGS_API = TASKMANAGER_API+"/$1/logs/";


  /** Others */
  public static final String FLINKOPTIONS = "flinkOptions";
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
  public static final ArrayList<String> MODES 
  = new ArrayList<String>(Arrays.asList(START,STOP,RESUME));


}
