package no.eira.relay.http;

import no.eira.relay.Constants;
import no.eira.relay.http.api.IHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpClientImpl implements IHttpClient {

    private static final int DEFAULT_MAX_RETRIES = 3;
    private static final int DEFAULT_INITIAL_DELAY_MS = 1000;

    @Override
    public String sendPost(String url, String parameters) {
        return sendPost(url, parameters, Collections.emptyMap());
    }

    @Override
    public String sendPost(String url, String parameters, Map<String, String> headers) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(parameters, StandardCharsets.UTF_8));

            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Constants.LOG.debug("HTTP POST {} - Status: {}", url, response.statusCode());
            return response.body();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            Constants.LOG.error("Failed to send POST request to {}: {}", url, e.getMessage());
        }
        return "";
    }

    @Override
    public String sendGet(String url, String parameters) {
        return sendGet(url, parameters, Collections.emptyMap());
    }

    @Override
    public String sendGet(String url, String parameters, Map<String, String> headers) {
        try {
            String fullUrl = parameters.isEmpty() ? url : url + "?" + parameters;
            URI uri = URI.create(fullUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET();

            for (Map.Entry<String, String> header : headers.entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Constants.LOG.debug("HTTP GET {} - Status: {}", fullUrl, response.statusCode());
            return response.body();
        } catch (IOException | InterruptedException e) {
            Constants.LOG.error("Failed to send GET request to {}: {}", url, e.getMessage());
        }
        return "";
    }

    /**
     * Send POST request with automatic retry on failure.
     * Uses exponential backoff: 1s, 2s, 4s delays between retries.
     *
     * @param url        Target URL
     * @param parameters JSON body
     * @param headers    Custom headers
     * @return CompletableFuture with response body (empty string on failure)
     */
    public CompletableFuture<String> sendPostWithRetry(String url, String parameters, Map<String, String> headers) {
        return sendPostWithRetry(url, parameters, headers, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY_MS);
    }

    /**
     * Send POST request with automatic retry on failure.
     *
     * @param url            Target URL
     * @param parameters     JSON body
     * @param headers        Custom headers
     * @param maxRetries     Maximum number of retry attempts
     * @param initialDelayMs Initial delay in milliseconds (doubles each retry)
     * @return CompletableFuture with response body (empty string on failure)
     */
    public CompletableFuture<String> sendPostWithRetry(String url, String parameters, Map<String, String> headers,
                                                        int maxRetries, int initialDelayMs) {
        return CompletableFuture.supplyAsync(() -> {
            int delay = initialDelayMs;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest.Builder builder = HttpRequest.newBuilder()
                            .uri(new URI(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(parameters, StandardCharsets.UTF_8));

                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        builder.header(header.getKey(), header.getValue());
                    }

                    HttpRequest request = builder.build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    // Check for success (2xx status codes)
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        Constants.LOG.debug("HTTP POST {} - Success on attempt {} (Status: {})",
                                url, attempt, response.statusCode());
                        return response.body();
                    }

                    // Log non-success status
                    Constants.LOG.warn("HTTP POST {} - Attempt {} failed with status {}",
                            url, attempt, response.statusCode());

                } catch (URISyntaxException | IOException | InterruptedException e) {
                    Constants.LOG.warn("HTTP POST {} - Attempt {} failed: {}",
                            url, attempt, e.getMessage());
                }

                // Wait before retry (unless this was the last attempt)
                if (attempt < maxRetries) {
                    try {
                        Constants.LOG.debug("Retrying in {}ms...", delay);
                        Thread.sleep(delay);
                        delay *= 2; // Exponential backoff
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            Constants.LOG.error("HTTP POST {} - Failed after {} attempts", url, maxRetries);
            return "";
        });
    }

    /**
     * Send GET request with automatic retry on failure.
     *
     * @param url        Target URL
     * @param parameters Query string parameters
     * @param headers    Custom headers
     * @return CompletableFuture with response body (empty string on failure)
     */
    public CompletableFuture<String> sendGetWithRetry(String url, String parameters, Map<String, String> headers) {
        return sendGetWithRetry(url, parameters, headers, DEFAULT_MAX_RETRIES, DEFAULT_INITIAL_DELAY_MS);
    }

    /**
     * Send GET request with automatic retry on failure.
     *
     * @param url            Target URL
     * @param parameters     Query string parameters
     * @param headers        Custom headers
     * @param maxRetries     Maximum number of retry attempts
     * @param initialDelayMs Initial delay in milliseconds (doubles each retry)
     * @return CompletableFuture with response body (empty string on failure)
     */
    public CompletableFuture<String> sendGetWithRetry(String url, String parameters, Map<String, String> headers,
                                                       int maxRetries, int initialDelayMs) {
        return CompletableFuture.supplyAsync(() -> {
            int delay = initialDelayMs;
            String fullUrl = parameters.isEmpty() ? url : url + "?" + parameters;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    URI uri = URI.create(fullUrl);
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest.Builder builder = HttpRequest.newBuilder()
                            .uri(uri)
                            .GET();

                    for (Map.Entry<String, String> header : headers.entrySet()) {
                        builder.header(header.getKey(), header.getValue());
                    }

                    HttpRequest request = builder.build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    // Check for success (2xx status codes)
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        Constants.LOG.debug("HTTP GET {} - Success on attempt {} (Status: {})",
                                fullUrl, attempt, response.statusCode());
                        return response.body();
                    }

                    // Log non-success status
                    Constants.LOG.warn("HTTP GET {} - Attempt {} failed with status {}",
                            fullUrl, attempt, response.statusCode());

                } catch (IOException | InterruptedException e) {
                    Constants.LOG.warn("HTTP GET {} - Attempt {} failed: {}",
                            fullUrl, attempt, e.getMessage());
                }

                // Wait before retry (unless this was the last attempt)
                if (attempt < maxRetries) {
                    try {
                        Constants.LOG.debug("Retrying in {}ms...", delay);
                        Thread.sleep(delay);
                        delay *= 2; // Exponential backoff
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            Constants.LOG.error("HTTP GET {} - Failed after {} attempts", fullUrl, maxRetries);
            return "";
        });
    }
}
