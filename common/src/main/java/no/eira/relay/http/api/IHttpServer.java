package no.eira.relay.http.api;

import java.io.IOException;

public interface IHttpServer {

    boolean startServer() throws IOException;
    void initHandlers();
    void stopServer();
    void registerHandler(IHttpHandler handler);
    void unregisterHandler(String url);
    IHttpHandler getHandlerByUrl(String url);
}
