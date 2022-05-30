package in.org.iudx.adaptor.utils;


import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;

import in.org.iudx.adaptor.logger.CustomLogger;
import in.org.iudx.adaptor.source.HttpSource;

import in.org.iudx.adaptor.codegen.ApiConfig;

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

    private HttpRequest.Builder requestBuilder;
    private HttpClient httpClient;
    private HttpRequest httpRequest;

    private String body;

    transient CustomLogger logger;


    /**
     * {@link HttpEntity} Constructor
     *
     * @param apiConfig The apiConfig to make requests and get responses
     *                  <p>
     *                  Note: This is called from context open() methods of the Source Function
     *                  <p>
     *                  TODO:
     *                   - Modularize/cleanup
     *                   - Handle timeouts from ApiConfig
     */
    public HttpEntity(ApiConfig apiConfig, String appName) {
        logger = new CustomLogger(HttpSource.class, appName);
        this.apiConfig = apiConfig;

        requestBuilder = HttpRequest.newBuilder();

        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        clientBuilder.version(Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(10));

        if (apiConfig.url != null) {
            requestBuilder.uri(URI.create(apiConfig.url));
        }
        /* TODO: consider making this neater */
        if (this.apiConfig.getHeaderString().length > 0) {
            requestBuilder.headers(this.apiConfig.headersArray);
        }
        httpClient = clientBuilder.build();
    }

    public HttpEntity setUrl(String url) {
        requestBuilder.uri(URI.create(url));
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

        if (apiConfig.requestType.equals("GET")) {
            httpRequest = requestBuilder.build();
        } else if (apiConfig.requestType.equals("POST")) {
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
            httpRequest = requestBuilder.POST(BodyPublishers.ofString(reqBody))
                    .build();
        }
        try {
            HttpResponse<String> resp =
                    httpClient.send(httpRequest, BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                logger.error("[HttpEntity] http request failed with {status_code: " + resp.statusCode() + "}");
                return "";
            }
            return resp.body();
        } catch (Exception e) {
            logger.error(e);
            return "";
        }
    }


    /*
     * TODO:
     * - Manage non-post requests
     * - Handle auth
     */
    public String postSerializedMessage(String message) throws IOException, InterruptedException {
        httpRequest = requestBuilder.POST(BodyPublishers.ofString(message)).build();
        try {
            HttpResponse<String> resp =
                    httpClient.send(httpRequest, BodyHandlers.ofString());
            return resp.body();
        } catch (Exception e) {
            throw e;
        }
    }
}
