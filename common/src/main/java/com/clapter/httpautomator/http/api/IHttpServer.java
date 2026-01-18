package com.clapter.httpautomator.http.api;

import com.clapter.httpautomator.http.handlers.HttpReceiverBlockHandler;

import java.io.IOException;

public interface IHttpServer {

    boolean startServer() throws IOException;
    void initHandlers();
    void stopServer();
    void registerHandler(IHttpHandler handler);
    IHttpHandler getHandlerByUrl(String url);
    void removeHandler(IHttpHandler handler);
    String getServerAddress();
    String getServerPublicAdress();
}
