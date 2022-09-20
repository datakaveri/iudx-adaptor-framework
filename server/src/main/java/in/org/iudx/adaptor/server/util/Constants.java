package in.org.iudx.adaptor.server.util;

import io.vertx.core.http.HttpMethod;

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

  /** RMQ **/
  public static final String RMQ_HOST = "rmqHost";
  public static final String RMQ_PORT = "rmqPort";
  public static final String RMQ_MANAGEMENT_PORT = "rmqMgmtPort";
  public static final String RMQ_VHOST = "rmqVhost";
  public static final String RMQ_USERNAME = "rmqUsername";
  public static final String RMQ_PASSWORD = "rmqPassword";

  /** Accept Headers and CORS */
  public static final String HEADER_ACCEPT = "Accept";
  public static final String HEADER_USERNAME = "username";
  public static final String HEADER_PASSWORD = "password";
  public static final String HEADER_GET = "GET";
  public static final String HEADER_POST = "POST";
  public static final String HEADER_OPTIONS = "OPTIONS";
  public static final String HEADER_TOKEN = "token";
  public static final String HEADER_CONTENT_LENGTH = "Content-Length";
  public static final String HEADER_CONTENT_TYPE = "Content-Type";
  public static final String HEADER_HOST = "Host";
  public static final String HEADER_INSTANCE = "instance";
  public static final String HEADER_ORIGIN = "Origin";
  public static final String HEADER_REFERER = "Referer";
  public static final String HEADER_CORS = "Access-Control-Allow-Origin";
  public static final Set<String> ALLOWED_HEADERS =
      new HashSet<String>(Arrays.asList(HEADER_ACCEPT, HEADER_USERNAME, HEADER_PASSWORD, HEADER_GET, HEADER_POST, HEADER_OPTIONS, HEADER_TOKEN, HEADER_CONTENT_LENGTH,
          HEADER_CONTENT_TYPE, HEADER_HOST, HEADER_ORIGIN, HEADER_REFERER, HEADER_CORS));

  public static final Set<HttpMethod> ALLOWED_METHODS = new HashSet<>(Arrays.asList(
    HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS, HttpMethod.DELETE, HttpMethod.PATCH, HttpMethod.PUT));


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
  public static final String RULE_ROUTE = "/rule";
  public static final String ADAPTOR_RULE_ROUTE = "/adaptor/:id/rules";

  public static final String ADAPTOR_RULE_DELETE_ROUTE = "/adaptor/:adaptorId/rules/:ruleId";
  public static final String ADAPTOR_ROUTE_ID = ADAPTOR_ROUTE + "/:id";
  public static final String ADAPTOR_START_ROUTE = ADAPTOR_ROUTE +"/:id/start";
  public static final String ADAPTOR_STOP_ROUTE = ADAPTOR_ROUTE +"/:id/stop";
  public static final String USER_ROUTE = "/user";
  public static final String USER_ROUTE_ID = USER_ROUTE + "/:id";

  /* Spec test routes */
  public static final String INPUT_SPEC_ROUTE = "/onboard/run-input-spec";
  public static final String PARSE_SPEC_ROUTE = "/onboard/run-parse-spec";
  public static final String TRANSFORM_SPEC_ROUTE = "/onboard/run-transform-spec";

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
  public static final String INVALID_SYNTAX = "invalidSyntax";
  public static final String DUPLICATE_ADAPTOR = "duplicateAdaptor";
  public static final String ALREADY_RUNNING = "alreadyRunning";
  public static final String NO_RUNNING_INS = "noRunningInstance";
  public static final String ADAPTOR_NOT_FOUND = "adaptorNotFound";
  public static final String INCOMPLETE_CODEGEN = "incompleteCodegen";

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

  /** Minio config params */
  public static final String MINIO_CONFIG = "minioConfig";
  public static final String MINIO_URL = "url";
  public static final String BUCKET = "bucket";
  public static final String STATE_NAME = "stateName";
  public static final String ACCESS_KEY = "accessKey";
  public static final String SECRET_KEY = "secretKey";

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
  public static final String INPUT_SPEC = "inputSpec";
  public static final String BOUNDED_JOB = "boundedJob";

  /* Modes */
  public static final String START = "start";
  public static final String STOP = "stop";
  public static final String RESUME = "resume";
  public static final ArrayList<String> MODES =
      new ArrayList<String>(Arrays.asList(START, STOP, RESUME));

  public static final long POLLING_INTEVAL = 60000L; // 1 Minute
  public static final String SCHEDULE_PATTERN = "schedulePattern";

  /* Database query */
  public static final String ADAPTOR_ID = "adaptorId";
  public static final String ADAPTOR_TYPE = "adaptorType";
  public static final String ADAPTOR_DEFAULT = "ETL";
  public static final String ADAPTOR_RULE = "RULES";
  public static final String ADAPTOR_ETL = "ETL";

  public static final String RULES = "rules";
  public static final String SOURCE_ID = "sourceId";
  public static final String RULE_TYPE = "ruleType";
  public static final String RULE_ID = "ruleId";
  public static final String JAR_ID = "jarId";
  public static final String JOB_ID = "jobId";
  public static final String COMPILING = "cg-compiling";
  public static final String SCHEDULED = "scheduled";
  public static final String COMPLETED = "cg-completed";
  public static final String CG_FAILED = "cg-failed";
  public static final String STOPPED = "stopped";
  public static final String RUNNING = "running";
  public static final String LASTSEEN = "lastSeen";
  public static final String TIMESTAMP = "timestamp";
  public static final String ADAPTORS = "adaptors";
  public static final String QUEUE_NAME = "queueName";
  public static final String ROUTING_KEY = "routingKey";
  public static final String RULE_NAME = "ruleName";
  public static final String EXCHANGE_NAME = "exchangeName";
  public static final String WINDOW_MINUTES = "windowMinutes";
  public static final String SQL_QUERY = "sqlQuery";
  public static final Set<String> ALLOWED_USER_STATUS =
      new HashSet<String>(Arrays.asList("active","inactive"));

  public static final String AUTHENTICATE_USER =
      "SELECT EXISTS ( SELECT * FROM public.\"user\" WHERE username = '$1' and password = '$2' and status = 'active')";
  public static final String CREATE_ADAPTOR =
      "INSERT into adaptor(adaptor_id,\"data\",\"timestamp\",user_id,adaptor_type) " +
      "SELECT '$1', '$2'::json, now(), us.\"id\", '$5' from public.\"user\" us where username = '$3';" +
      "INSERT into codegen_status(status, \"timestamp\", adaptor_id) values ('$4', now(),'$1')";

  public static final String CREATE_RULESOURCE =
      "INSERT into rulesource(adaptor_id,source_id,ruleexchange,rulequeue,\"timestamp\",user_id)" +
      "SELECT '$1', '$2', '$3', '$4', now(), us.\"id\" from public.\"user\" us where username = '$5';";

  public static final String CREATE_RULE =
      "INSERT into rules(adaptor_id,source_id,exchangename,queuename,routingkey,sqlquery,windowminutes,ruletype, user_id,rule_name) VALUES('$1', (select \"source_id\" from rulesource  where adaptor_id='$1'), '$2', '$3', '$4', '$5', $6, '$7', (select \"id\" from public.user where username = '$8'), '$9') returning id;";

  public static final String UPDATE_STATUS = "INSERT into codegen_status(status, \"timestamp\", adaptor_id) values ('$1', now(),'$2')";

  public static final String UPDATE_JARID = "UPDATE adaptor SET jar_id = '$1' WHERE adaptor_id = '$2'";

  public static final String DELETE_ADAPTOR = "DELETE FROM adaptor where adaptor_id = '$1'";

  public static final String UPDATE_COMPLEX = "WITH update_adaptor AS (\n" +
      "  UPDATE adaptor SET jar_id = '$1' WHERE adaptor_id = '$2'\n" +
      "  returning adaptor_id\n" +
      ")\n" +
      "UPDATE codegen_status SET status = '$3', \"timestamp\" = now() \n" +
      "  FROM (select adaptor_id from update_adaptor) AS adaptor\n" +
      "  WHERE codegen_status.adaptor_id = adaptor.adaptor_id";

  public static final String UPDATE_COMPLEX_PARTIAL = "WITH update_adaptor AS (\n" +
      "  UPDATE adaptor SET jar_id = '$1' WHERE adaptor_id = '$2'\n" +
      "  returning adaptor_id\n" +
      ")\n" +
      "UPDATE codegen_status SET status = '$3', \"timestamp\" = now() \n" +
      "  FROM (select adaptor_id from update_adaptor) AS adaptor\n" +
      "  WHERE codegen_status.adaptor_id = adaptor.adaptor_id";

  public static final String INSERT_JOB =
      "INSERT into flink_job(job_id, \"timestamp\",status,adaptor_id) values ('$1',now(),'$2','$3')";
  public static final String SELECT_JOB ="SELECT job_id FROM flink_job WHERE adaptor_id='$1' AND status = '$2'";
  public static final String SELECT_ALL_JOBS = "SELECT job_id FROM flink_job where status ='running'";
  public static final String UPDATE_JOB = "update flink_job set status ='$2', timestamp = now() where job_id = '$1'";

  public static final String GET_ALL_ADAPTOR =
      "WITH get_user_adaptor AS (\n" +
      "  SELECT ad.adaptor_id, ad.jar_id, fj.job_id, ad.data, ad.adaptor_type\n," +
      "    COALESCE(fj.timestamp, cg.timestamp) AS timestamp, COALESCE (fj.status, cg.status) AS status\n" +
      "    FROM adaptor AS ad\n" +
      "    INNER JOIN public.user AS _user ON ad.user_id = _user.id\n" +
      "        AND _user.id = (SELECT id FROM public.user WHERE username = '$1')\n" +
      "    LEFT JOIN codegen_status AS cg ON ad.adaptor_id = cg.adaptor_id\n" +
      "    LEFT JOIN flink_job AS fj ON cg.adaptor_id = fj.adaptor_id\n" +
      "),\n" +
      " get_filter_job AS (\n" +
      "     SELECT * FROM get_user_adaptor t1 \n" +
      "     WHERE timestamp = (SELECT MAX(timestamp) FROM get_user_adaptor t2 WHERE t1.adaptor_id = t2.adaptor_id)\n" +
      ")\n" +
      "SELECT * FROM get_filter_job ORDER BY timestamp DESC";

  public static final String GET_ONE_ADAPTOR =
      "WITH get_user_adaptor AS (\n" +
      "  SELECT ad.adaptor_id, ad.jar_id, fj.job_id, ad.data, ad.adaptor_type\n," +
      "    COALESCE(fj.timestamp, cg.timestamp) AS timestamp, COALESCE (fj.status, cg.status) AS status\n" +
      "    FROM adaptor AS ad\n" +
      "    INNER JOIN public.user AS _user ON ad.user_id = _user.id\n" +
      "        AND _user.id = (SELECT id FROM public.user WHERE username = '$1')\n" +
      "    LEFT JOIN codegen_status AS cg ON ad.adaptor_id = cg.adaptor_id\n" +
      "    LEFT JOIN flink_job AS fj ON cg.adaptor_id = fj.adaptor_id\n" +
      "),\n" +
      " get_filter_job AS (\n" +
      "     SELECT * FROM get_user_adaptor t1 \n" +
      "     WHERE timestamp = (SELECT MAX(timestamp) FROM get_user_adaptor t2 WHERE t1.adaptor_id = t2.adaptor_id)\n" +
      "     AND adaptor_id = '$2'\n" +
      ")\n" +
      "SELECT * FROM get_filter_job";

  /* TODO Better query */
  public static final String GET_ALL_RULE_SOURCES =
    "select rs.adaptor_id,rs.source_id,rs.ruleexchange,rs.rulequeue,rs.timestamp,us.username," +
    "us.status from rulesource rs  inner join public.user us on rs.user_id = us.id where us.username = '$1'";

  public static final String GET_RULE_SOURCE_FROM_ADAPTOR_ID =
    "select rs.adaptor_id,rs.source_id,rs.ruleexchange,rs.rulequeue,rs.timestamp,us.username," +
    "us.status from rulesource rs  inner join public.user us on rs.user_id = us.id where us.username = '$1' and rs.adaptor_id = '$2'";

  public static final String GET_RULES_FROM_ADAPTOR_ID =
    "select rule.id, rule.rule_name,rule.adaptor_id,rule.exchangename,rule.queuename,rule.timestamp," +
    "rule.sqlquery,us.username,us.status from rules rule  inner join public.user us on\n" +
    "rule.user_id = us.id where us.username = '$1' and rule.adaptor_id = '$2'";

  public static final String DELETE_RULE = "DELETE FROM rules where adaptor_id = '$1'\n" +
          "and id = '$2'";
  public static final String REGISTER_USER = "INSERT INTO public.user "
      + "(username, password, status,\"timestamp\") VALUES ('$1','$2','$3',now());";

  public static final String UPDATE_USER = "UPDATE public.user SET password='$2',status='$3',\"timestamp\"=now() WHERE username = '$1'";
  public static final String UPDATE_USER_PASSWORD = "UPDATE public.user SET password='$2',\"timestamp\"=now() WHERE username = '$1'";
  public static final String UPDATE_USER_STATUS = "UPDATE public.user SET status = '$2',\"timestamp\"=now() where username = '$1'";

  public static final String GET_USERS = "SELECT username,password,status,\"timestamp\" FROM public.user";
  public static final String GET_USER = "SELECT username,password,status,\"timestamp\" FROM public.user WHERE username = '$1'";
}
