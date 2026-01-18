package com.clapter.httpautomator.platform.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class HttpServerConfig implements IHttpServerConfig {

    public static List<GlobalParam> globalParams = new ArrayList<>();
    public static String globalRedirect;
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("happyhttp-global-vars.toml");
    private static final File CONFIG_FILE = CONFIG_PATH.toFile();

    private static ForgeConfigSpec.IntValue PORT;
    private static ForgeConfigSpec.ConfigValue LOCAL_ADRESS;

    static {

        COMMON_BUILDER.push("Http Server Settings");

        PORT = COMMON_BUILDER
                .comment("Http Server Port")
                .defineInRange("port", 8080, 0, 999999);

        LOCAL_ADRESS = COMMON_BUILDER
                .comment("Local adress of the machine. Leave empty to determine automatically (May be wrong if more than one Network Interface)")
                .define("local_adress", "192.168.0.1");


        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            createDefaultConfig();
        }
        readConfig();
    }

    private static void createDefaultConfig() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE))) {
            writer.write("# Global parameters");
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write("# Global parameters are parameters like the configurable in the block, but applies to all blocks.");
            writer.newLine();
            writer.write("# The parameters will be checked like the other parameters.");
            writer.newLine();
            writer.write("# Instead of manually adding special parameters to all blocks, global parameters can be configured.");
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write("# The webhook will use both global parameters and the parameters configured for the block when submitting the URL");
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write("# EXAMPLE of webhook with global parameters");
            writer.newLine();
            writer.write("# http://webhookURL/endpoint$game=scavengerhunt2&team=kickstarters&controller=j6fjjy5glkujgtfgjhh765gj98hgmkgh6");
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write("# URL to send if the parameters are missing (there is no global parameter in the URL)");
            writer.newLine();
            writer.write("# Typically this will send a message like: Curious about this code? Find information about the game here.");
            writer.newLine();
            writer.write("global_param_redirect_missing = http://www.myhost.com/redirectMissing.html");
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write("# global_param.ID = parameter_name");
            writer.newLine();
            writer.write("# global_param.value = parameter_value");
            writer.newLine();
            writer.write("# global_param.redirect_wrong = URL");
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
                if (key.startsWith("global_param.")) {
                    String[] parts = key.split("\\.");
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
                if(key.startsWith("global_param_redirect")) {
                    globalRedirect = properties.getProperty(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public HttpServerConfig() {
    }

    @Override
    public int getPort() {
        return PORT.get();
    }

    @Override
    public String getLocalAdress() {
        return LOCAL_ADRESS.get().toString();
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
