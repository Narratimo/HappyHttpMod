package com.clapter.httpautomator.platform.config;

import com.clapter.httpautomator.platform.config.IHttpServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HttpServerConfig implements IHttpServerConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final HttpServerConfig INSTANCE;
    
    private static ForgeConfigSpec.ConfigValue<Integer> port;

    static {
        Pair<HttpServerConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(HttpServerConfig::new);
        INSTANCE = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    // Required no-arg constructor for ServiceLoader
    public HttpServerConfig() {
    }

    // Constructor used by ForgeConfigSpec.Builder
    public HttpServerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("Http Server Settings");

        port = builder
                .comment("Http Server Port")
                .defineInRange("port", 8080, 0, 65535);

        builder.pop();
    }

    @Override
    public int getPort() {
        return port != null ? port.get() : 8080;
    }

}
