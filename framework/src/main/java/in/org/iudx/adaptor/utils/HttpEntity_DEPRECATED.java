
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
public class HttpEntity_DEPRECATED {

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
     *                                                    TODO:
     *                                                     - Modularize/cleanup
     *                                                     - Handle timeouts from ApiConfig
     */
    public HttpEntity_DEPRECATED(ApiConfig apiConfig, String appName) {
        logger = new CustomLogger(HttpSource.class, appName);
        this.apiConfig = apiConfig;

        requestBuilder = HttpRequest.newBuilder();

        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        clientBuilder.version(Version.HTTP_2).connectTimeout(Duration.ofSeconds(10));

        if (apiConfig.url != null) {
            requestBuilder.uri(URI.create(apiConfig.url));
        }
        /* TODO: consider making this neater */
        if (this.apiConfig.getHeaderString_DEPRECATED().length > 0) {
            requestBuilder.headers(this.apiConfig.getHeaderString_DEPRECATED());
        }
        httpClient = clientBuilder.build();
    }

    public HttpEntity_DEPRECATED(ApiConfig apiConfig) {
        logger = new CustomLogger(HttpSource.class);
        this.apiConfig = apiConfig;

        requestBuilder = HttpRequest.newBuilder();

        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        clientBuilder.version(Version.HTTP_2).connectTimeout(Duration.ofSeconds(10));

        if (apiConfig.url != null) {
            requestBuilder.uri(URI.create(apiConfig.url));
        }
        /* TODO: consider making this neater */
        if (this.apiConfig.getHeaderString_DEPRECATED().length > 0) {
            requestBuilder.headers(this.apiConfig.getHeaderString_DEPRECATED());
        }
        httpClient = clientBuilder.build();
    }

    public HttpEntity_DEPRECATED setUrl(String url) {
        requestBuilder.uri(URI.create(url));
        return this;
    }

    public HttpEntity_DEPRECATED setBody(String body) {
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
        requestBuilder.timeout(Duration.ofSeconds(apiConfig.requestTimeout));
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

            if (resp.statusCode() / 100 != 2) {
                logger.error("[status_code - " + resp.statusCode() + "] - [summary - " + resp.body() + "] Http request failed");
                return "";
            }
            return resp.body();
        } catch (Exception e) {
            logger.error("Error http entity", e);
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
            logger.error("Error in post request", e);
            throw e;
        }
    }
}