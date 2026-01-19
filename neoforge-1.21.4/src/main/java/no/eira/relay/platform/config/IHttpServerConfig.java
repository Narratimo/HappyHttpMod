package no.eira.relay.platform.config;

import java.util.List;

public interface IHttpServerConfig {

    int getPort();

    List<GlobalParam> getGlobalParams();

    String getGlobalRedirect();

    // Rate limiting
    boolean isRateLimitEnabled();
    int getRateLimitPerMinute();

    // Authentication
    boolean requireAuth();
    List<String> getApiKeys();

    // CORS
    boolean isCorsEnabled();
    List<String> getCorsOrigins();
}

