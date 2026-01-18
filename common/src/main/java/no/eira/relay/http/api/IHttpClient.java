package no.eira.relay.http.api;

public interface IHttpClient {

    String sendPost(String url, String parameters);
    String sendGet(String url, String parameters);
}
