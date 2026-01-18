package com.clapter.httpautomator.platform.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HttpServerConfig implements IHttpServerConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final HttpServerConfig INSTANCE;

    private final ForgeConfigSpec.IntValue port;

    static {
        Pair<HttpServerConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder()
                .configure(HttpServerConfig::new);
        INSTANCE = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    // Constructor used by ForgeConfigSpec.Builder
    private HttpServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Http Server Settings");
        port = builder
                .comment("Http Server Port")
                .defineInRange("port", 8080, 0, 65535);
        builder.pop();
    }

    /**
     * Public zero-argument constructor required by the service loader.
     * Reuses the statically created instance so the Forge config spec is shared.
     */
    public HttpServerConfig() {
        this.port = INSTANCE.port;
    }

    @Override
    public int getPort() {
        return port.get();
    }
}
