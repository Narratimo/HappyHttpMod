package com.clapter.httpautomator.http;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.http.api.IHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HttpClientImpl implements IHttpClient {

    @Override
    public String sendPost(String url, String parameters) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(parameters, StandardCharsets.UTF_8))
                    .build();

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
        try {
            String fullUrl = parameters.isEmpty() ? url : url + "?" + parameters;
            URI uri = URI.create(fullUrl);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Constants.LOG.debug("HTTP GET {} - Status: {}", fullUrl, response.statusCode());
            return response.body();
        } catch (IOException | InterruptedException e) {
            Constants.LOG.error("Failed to send GET request to {}: {}", url, e.getMessage());
        }
        return "";
    }
}
