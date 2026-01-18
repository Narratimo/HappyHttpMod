package no.eira.relay.platform.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class HttpServerConfig implements IHttpServerConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final HttpServerConfig INSTANCE;
    
    private static ModConfigSpec.ConfigValue<Integer> port;

    static {
        Pair<HttpServerConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(HttpServerConfig::new);
        INSTANCE = pair.getLeft();
        COMMON_SPEC = pair.getRight();
    }

    // Required no-arg constructor for ServiceLoader
    public HttpServerConfig() {
    }

    // Constructor used by ModConfigSpec.Builder
    public HttpServerConfig(ModConfigSpec.Builder builder) {
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

