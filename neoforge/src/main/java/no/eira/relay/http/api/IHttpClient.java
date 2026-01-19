package no.eira.relay.http.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IHttpClient {

    String sendPost(String url, String parameters);

    String sendPost(String url, String parameters, Map<String, String> headers);

    String sendGet(String url, String parameters);

    String sendGet(String url, String parameters, Map<String, String> headers);

    /**
     * Send POST request with automatic retry on failure.
     * @param url Target URL
     * @param parameters JSON body
     * @param headers Custom headers
     * @return CompletableFuture with response body
     */
    CompletableFuture<String> sendPostWithRetry(String url, String parameters, Map<String, String> headers);

    /**
     * Send GET request with automatic retry on failure.
     * @param url Target URL
     * @param parameters Query string parameters
     * @param headers Custom headers
     * @return CompletableFuture with response body
     */
    CompletableFuture<String> sendGetWithRetry(String url, String parameters, Map<String, String> headers);

    /**
     * Send POST request with automatic retry, returning full result with status code.
     * @param url Target URL
     * @param parameters JSON body
     * @param headers Custom headers
     * @return CompletableFuture with HttpResult containing status code and body
     */
    CompletableFuture<HttpResult> sendPostWithResult(String url, String parameters, Map<String, String> headers);

    /**
     * Send GET request with automatic retry, returning full result with status code.
     * @param url Target URL
     * @param parameters Query string parameters
     * @param headers Custom headers
     * @return CompletableFuture with HttpResult containing status code and body
     */
    CompletableFuture<HttpResult> sendGetWithResult(String url, String parameters, Map<String, String> headers);
}
