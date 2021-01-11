package in.org.iudx.adaptor.server;

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
  public static final Set<String> ALLOWED_HEADERS
    = new HashSet<String>(Arrays.asList(HEADER_ACCEPT, HEADER_TOKEN, HEADER_CONTENT_LENGTH,
                                    HEADER_CONTENT_TYPE, HEADER_HOST, HEADER_ORIGIN,
                                    HEADER_REFERER, HEADER_CORS));

  public static final String MIME_APPLICATION_JSON = "application/json";
  public static final String MIME_TEXT_HTML = "text/html";


}
