package no.eira.relay.http.api;

import com.sun.net.httpserver.HttpHandler;

import java.util.List;

public interface IHttpHandler extends HttpHandler {

    String getUrl();
    List<String> httpMethods();
    default void handle(){
        //DEFAULT IMPLEMENTATION
    }
}
