package in.org.iudx.adaptor.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.source.HttpSource;

import in.org.iudx.adaptor.codegen.ApiConfig;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.hc.core5.util.Timeout;
import org.json.JSONObject;
import org.json.XML;

/**
 * {@link HttpEntity} - Http requests/response handler
 * This class handles all http requests.
 * <p>
 * Note:
 * - ?This is serializable from examples encountered.
 * <p>
 * <p>
 * Todos:
 * - Make connection/etc closeable
 * - Configurable timeouts
 * - Parse response bodies
 */
public class HttpEntity {

    public ApiConfig apiConfig;

    private final HttpClientBuilder clientBuilder;

    private ClassicRequestBuilder requestBuilder;

    private final CloseableHttpClient httpClient;

    private String body;

    CustomLogger logger;

    /**
     * {@link HttpEntity} Constructor
     *
     * @param apiConfig The apiConfig to make requests and get responses
     *                  <p>
     *                  Note: This is called from context open() methods of the Source Function
     *                  <p>
     */
    public HttpEntity(ApiConfig apiConfig, String appName) {
        logger = new CustomLogger(HttpSource.class, appName);
        this.apiConfig = apiConfig;

        clientBuilder = HttpClients.custom();

        if (apiConfig.requestType != null) {
            requestBuilder = ClassicRequestBuilder.create(apiConfig.requestType);
        }

        if (apiConfig.url != null) {
            requestBuilder.setUri(apiConfig.url);
        }

        if (this.apiConfig.getHeaders().length != 0) {
            requestBuilder.setHeaders(this.apiConfig.getHeaders());
        }

        RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setConnectTimeout(Timeout.ofSeconds(apiConfig.requestTimeout));
        configBuilder.setConnectionRequestTimeout(Timeout.ofSeconds(apiConfig.requestTimeout));
        configBuilder.setResponseTimeout(Timeout.ofSeconds(apiConfig.requestTimeout));
        RequestConfig config = configBuilder.build();
        clientBuilder.setDefaultRequestConfig(config);
        httpClient = clientBuilder.build();
    }

    public HttpEntity(ApiConfig apiConfig) {
        logger = new CustomLogger(HttpSource.class);
        this.apiConfig = apiConfig;

        clientBuilder = HttpClients.custom();

        if (apiConfig.requestType != null) {
            requestBuilder = ClassicRequestBuilder.create(apiConfig.requestType);
        }

        if (apiConfig.url != null) {
            requestBuilder.setUri(apiConfig.url);
        }

        if (this.apiConfig.getHeaders().length != 0) {
            requestBuilder.setHeaders(this.apiConfig.getHeaders());
        }

        RequestConfig.Builder configBuilder = RequestConfig.custom();
        configBuilder.setConnectTimeout(Timeout.ofSeconds(apiConfig.requestTimeout));
        configBuilder.setConnectionRequestTimeout(Timeout.ofSeconds(apiConfig.requestTimeout));
        configBuilder.setResponseTimeout(Timeout.ofSeconds(apiConfig.requestTimeout));
        RequestConfig config = configBuilder.build();
        clientBuilder.setDefaultRequestConfig(config);
        httpClient = clientBuilder.build();
    }

    public HttpEntity setUrl(String url) {
        requestBuilder.setUri(url);
        return this;
    }

    public HttpEntity setBody(String body) {
        this.body = body;
        return this;
    }

    public ApiConfig getApiConfig() {
        return this.apiConfig;
    }


    /**
     * Get the response message as a string
     * <p>
     * Note:
     * - This is the method which deals with responses Raw
     */
    public String getSerializedMessage() {
        if(apiConfig.responseType.equals(ContentType.APPLICATION_XML.getMimeType())) {
          ClassicHttpRequest httpRequest = requestBuilder.build();
          try(ClassicHttpResponse resp = httpClient.execute(httpRequest)) {
            org.apache.hc.core5.http.HttpEntity entity = resp.getEntity();

            if (entity != null) {
              byte[] bytesArray = EntityUtils.toByteArray(entity);
              String responseBody = new String(bytesArray, StandardCharsets.UTF_8);

              JSONObject jsonObject = XML.toJSONObject(responseBody);
              return jsonObject.toString();
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }

          return "";
        } else {
          if (apiConfig.requestType.equals("POST")) {
            String reqBody = "";
            if (this.body == null) {
              if (apiConfig.body == null) {
                return "";
              } else {
                reqBody = apiConfig.body;
              }
            } else {
              reqBody = apiConfig.body;
            }

            requestBuilder.setEntity(reqBody, ContentType.APPLICATION_JSON);
          }

          ClassicHttpRequest httpRequest = requestBuilder.build();
          try (ClassicHttpResponse resp = httpClient.execute(httpRequest)) {
            org.apache.hc.core5.http.HttpEntity entity = resp.getEntity();

            if (entity != null) {
              byte[] bytesArray = EntityUtils.toByteArray(entity);
              int contentLength = bytesArray.length;
              String responseBody = new String(bytesArray, StandardCharsets.UTF_8);
              logger.info("[status_code - " + resp.getCode() + "] - [summary - status response " + resp.getReasonPhrase() + "] API with response of size " + contentLength + " bytes");
              if (resp.getCode() / 100 != 2) {
                logger.error("[status_code - " + resp.getCode() + "] - [summary - " + responseBody + "] Http request failed");
                return "";
              }
              return responseBody;
            }
            return "";
          } catch (Exception e) {
            logger.error("Error http entity", e);
            return "";
          }
        }
    }


    /*
     * TODO:
     * - Manage non-post requests
     * - Handle auth
     */
    public String postSerializedMessage(String message) throws IOException {
        requestBuilder.setEntity(message, ContentType.APPLICATION_JSON);
        ClassicHttpRequest httpRequest = requestBuilder.build();
        try (ClassicHttpResponse resp = httpClient.execute(httpRequest)) {
            org.apache.hc.core5.http.HttpEntity entity = resp.getEntity();
            if (entity != null) {
                return IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error in post request", e);
            throw e;
        }
    }
}
