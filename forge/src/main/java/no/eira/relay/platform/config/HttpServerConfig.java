package no.eira.relay.platform.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HttpServerConfig implements IHttpServerConfig {

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final HttpServerConfig INSTANCE;

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("eirarelay-global-vars.toml");
    private static final File CONFIG_FILE = CONFIG_PATH.toFile();

    private static List<GlobalParam> globalParams = new ArrayList<>();
    private static String globalRedirect;

    private final ForgeConfigSpec.IntValue port;

    // Rate limiting
    private final ForgeConfigSpec.BooleanValue rateLimitEnabled;
    private final ForgeConfigSpec.IntValue rateLimitPerMinute;

    // Authentication
    private final ForgeConfigSpec.BooleanValue requireAuth;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> apiKeys;

    // CORS
    private final ForgeConfigSpec.BooleanValue corsEnabled;
    private final ForgeConfigSpec.ConfigValue<List<? extends String>> corsOrigins;

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

        builder.push("Rate Limiting");
        rateLimitEnabled = builder
                .comment("Enable rate limiting for HTTP requests")
                .define("enabled", false);
        rateLimitPerMinute = builder
                .comment("Maximum requests per minute per IP address")
                .defineInRange("requestsPerMinute", 100, 1, 10000);
        builder.pop();

        builder.push("Authentication");
        requireAuth = builder
                .comment("Require API key authentication for all requests")
                .define("requireAuth", false);
        apiKeys = builder
                .comment("List of valid API keys")
                .defineList("apiKeys", List.of(), obj -> obj instanceof String);
        builder.pop();

        builder.push("CORS");
        corsEnabled = builder
                .comment("Enable CORS headers for cross-origin requests")
                .define("enabled", false);
        corsOrigins = builder
                .comment("Allowed origins for CORS (use * for all)")
                .defineList("origins", List.of("*"), obj -> obj instanceof String);
        builder.pop();
    }

    /**
     * Public zero-argument constructor required by the service loader.
     * Reuses the statically created instance so the Forge config spec is shared.
     */
    public HttpServerConfig() {
        this.port = INSTANCE.port;
        this.rateLimitEnabled = INSTANCE.rateLimitEnabled;
        this.rateLimitPerMinute = INSTANCE.rateLimitPerMinute;
        this.requireAuth = INSTANCE.requireAuth;
        this.apiKeys = INSTANCE.apiKeys;
        this.corsEnabled = INSTANCE.corsEnabled;
        this.corsOrigins = INSTANCE.corsOrigins;
    }

    public static void loadGlobalParamsConfig() {
        if (!CONFIG_FILE.exists()) {
            createDefaultConfig();
        }
        readConfig();
    }

    private static void createDefaultConfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            writer.write("# Global parameters for Eira Relay");
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write("# Global parameters are checked on every HTTP request to receiver blocks.");
            writer.newLine();
            writer.write("# If a global parameter is missing or has the wrong value, the request");
            writer.newLine();
            writer.write("# can be redirected to a specified URL.");
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write("# URL to redirect to if no parameters are present:");
            writer.newLine();
            writer.write("global_param_redirect_missing = ");
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write("# Define global parameters like this:");
            writer.newLine();
            writer.write("# global_param.1.name = game");
            writer.newLine();
            writer.write("# global_param.1.value = scavengerhunt");
            writer.newLine();
            writer.write("# global_param.1.redirect_wrong = http://example.com/wrong-game");
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readConfig() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Properties properties = new Properties();
            properties.load(reader);

            globalParams.clear();

            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("global_param.") && !key.startsWith("global_param_redirect")) {
                    String[] parts = key.split("\\.");
                    if (parts.length >= 3) {
                        String paramIndex = parts[1];
                        String paramKey = parts[2];

                        GlobalParam param = globalParams.stream()
                                .filter(p -> p.index.equals(paramIndex))
                                .findFirst()
                                .orElseGet(() -> {
                                    GlobalParam newParam = new GlobalParam(paramIndex);
                                    globalParams.add(newParam);
                                    return newParam;
                                });

                        switch (paramKey) {
                            case "name":
                                param.name = properties.getProperty(key);
                                break;
                            case "value":
                                param.value = properties.getProperty(key);
                                break;
                            case "redirect_wrong":
                                param.redirectWrong = properties.getProperty(key);
                                break;
                        }
                    }
                }
                if (key.startsWith("global_param_redirect")) {
                    globalRedirect = properties.getProperty(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getPort() {
        return port.get();
    }

    @Override
    public List<GlobalParam> getGlobalParams() {
        return globalParams;
    }

    @Override
    public String getGlobalRedirect() {
        return globalRedirect;
    }

    @Override
    public boolean isRateLimitEnabled() {
        return rateLimitEnabled.get();
    }

    @Override
    public int getRateLimitPerMinute() {
        return rateLimitPerMinute.get();
    }

    @Override
    public boolean requireAuth() {
        return requireAuth.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getApiKeys() {
        return (List<String>) apiKeys.get();
    }

    @Override
    public boolean isCorsEnabled() {
        return corsEnabled.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getCorsOrigins() {
        return (List<String>) corsOrigins.get();
    }
}
