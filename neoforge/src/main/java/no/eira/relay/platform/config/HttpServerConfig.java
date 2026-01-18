package no.eira.relay.platform.config;

import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HttpServerConfig implements IHttpServerConfig {

    public static final ModConfigSpec COMMON_SPEC;
    public static final HttpServerConfig INSTANCE;

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("eirarelay-global-vars.toml");
    private static final File CONFIG_FILE = CONFIG_PATH.toFile();

    private static List<GlobalParam> globalParams = new ArrayList<>();
    private static String globalRedirect;

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
        return port != null ? port.get() : 8080;
    }

    @Override
    public List<GlobalParam> getGlobalParams() {
        return globalParams;
    }

    @Override
    public String getGlobalRedirect() {
        return globalRedirect;
    }
}

