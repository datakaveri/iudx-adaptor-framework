package in.org.iudx.adaptor.codegen;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;


/**
 * {@link ApiConfig} - Api configuration class
 * Encapsulates api information such as making a url,
 * defining request type, adding headers etc.
 * Note: This must be serializable since flink passes
 * this accross its instances.
 * <p>
 * TODO:
 *  - Extend as needed
 *  - Jackson databind
 */
public class ApiConfig implements Serializable {
    public String url;
    public String body;
    public String requestType = "GET";
    public long requestTimeout = 10;
    public long pollingInterval;
    public boolean hasScript = false;
  public ContentType responseType = ContentType.APPLICATION_JSON;

    public Map<String,String> headers_DEPRECATED = new HashMap<String,String>();
    public List<Header> headers = new ArrayList<Header>();
//    public Map<BasicHeader> headers = new HashMap<BasicHeader>();
    public ArrayList<HashMap<String, String>> scripts
            = new ArrayList<HashMap<String, String>>();

    private static final long serialVersionUID = 2L;

    public ApiConfig() {
    }

    public ApiConfig setUrl(String url) {
        this.url = url;
        return this;
    }

    public ApiConfig setBody(String body) {
        this.body = body;
        return this;
    }

    public ApiConfig setHeader(String key, String value) {
        Header header = new BasicHeader(key, value);
        headers.add(header);
        return this;
    }

    public ApiConfig setHeader_DEPRECATED(String key, String value) {
        headers_DEPRECATED.put(key, value);
        return this;
    }

    public ApiConfig setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public ApiConfig setPollingInterval(long pollingInterval) {
        this.pollingInterval = pollingInterval;
        return this;
    }

    /* type is get or post */
    public ApiConfig setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public ApiConfig setResponseType(ContentType contentType) {
      this.responseType = contentType;
      return this;
    }

    public String[] getHeaderString_DEPRECATED() {
        List<String> headerList = new ArrayList<String>();
        headers_DEPRECATED.forEach((k,v) -> {
            headerList.add(k);
            headerList.add(v);
        });
        return headerList.toArray(new String[0]);
    }

    public Header[] getHeaders() {
        return headers.toArray(Header[]::new);
    }

    public ApiConfig setParamGenScript(String in, String pattern, String script) {
        hasScript = true;

        HashMap<String, String> mp = new HashMap<String, String>();
        mp.put("in", in);
        mp.put("pattern", pattern);
        mp.put("script", script);
        scripts.add(mp);
        return this;
    }

    public ApiConfig buildConfig() {
        return this;
    }

}
