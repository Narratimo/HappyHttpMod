package no.eira.relay.http.api;

import java.util.Map;

public interface IHttpClient {

    String sendPost(String url, String parameters);

    String sendPost(String url, String parameters, Map<String, String> headers);

    String sendGet(String url, String parameters);

    String sendGet(String url, String parameters, Map<String, String> headers);
}
