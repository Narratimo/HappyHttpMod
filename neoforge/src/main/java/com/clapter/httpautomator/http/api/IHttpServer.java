package com.clapter.httpautomator.http.api;

import java.io.IOException;

public interface IHttpServer {

    boolean startServer() throws IOException;
    void initHandlers();
    void stopServer();
    void registerHandler(IHttpHandler handler);
    IHttpHandler getHandlerByUrl(String url);
}
