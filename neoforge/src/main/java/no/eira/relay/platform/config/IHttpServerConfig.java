package no.eira.relay.platform.config;

import java.util.List;

public interface IHttpServerConfig {

    int getPort();

    List<GlobalParam> getGlobalParams();

    String getGlobalRedirect();

}

