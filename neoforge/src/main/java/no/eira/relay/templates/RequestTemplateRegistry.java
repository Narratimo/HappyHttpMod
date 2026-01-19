package no.eira.relay.templates;

import no.eira.relay.enums.EnumAuthType;
import no.eira.relay.enums.EnumHttpMethod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of pre-configured request templates for common integrations.
 */
public class RequestTemplateRegistry {

    private static final Map<String, RequestTemplate> TEMPLATES = new LinkedHashMap<>();

    static {
        // Initialize built-in templates
        registerBuiltInTemplates();
    }

    private static void registerBuiltInTemplates() {
        // Discord Webhook
        register(RequestTemplate.builder("discord_webhook")
            .name("Discord Webhook")
            .description("Send messages to Discord channel")
            .urlPattern("https://discord.com/api/webhooks/{webhook_id}/{webhook_token}")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.NONE)
            .parameter("content", "Message from Minecraft!")
            .build());

        // Discord Rich Embed
        register(RequestTemplate.builder("discord_embed")
            .name("Discord Embed")
            .description("Send rich embed to Discord")
            .urlPattern("https://discord.com/api/webhooks/{webhook_id}/{webhook_token}")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.NONE)
            .parameter("embeds", "[{\"title\":\"Event\",\"description\":\"Something happened!\",\"color\":5814783}]")
            .build());

        // IFTTT Webhook
        register(RequestTemplate.builder("ifttt_webhook")
            .name("IFTTT Webhook")
            .description("Trigger IFTTT applet")
            .urlPattern("https://maker.ifttt.com/trigger/{event}/with/key/{key}")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.NONE)
            .parameter("value1", "")
            .parameter("value2", "")
            .parameter("value3", "")
            .build());

        // Home Assistant Webhook
        register(RequestTemplate.builder("homeassistant_webhook")
            .name("Home Assistant Webhook")
            .description("Trigger Home Assistant automation")
            .urlPattern("http://{host}:8123/api/webhook/{webhook_id}")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.NONE)
            .parameter("event", "minecraft_trigger")
            .build());

        // Home Assistant API
        register(RequestTemplate.builder("homeassistant_api")
            .name("Home Assistant API")
            .description("Call Home Assistant service")
            .urlPattern("http://{host}:8123/api/services/{domain}/{service}")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.BEARER)
            .parameter("entity_id", "")
            .build());

        // Slack Webhook
        register(RequestTemplate.builder("slack_webhook")
            .name("Slack Webhook")
            .description("Send message to Slack channel")
            .urlPattern("https://hooks.slack.com/services/{path}")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.NONE)
            .parameter("text", "Message from Minecraft!")
            .build());

        // Generic REST API GET
        register(RequestTemplate.builder("rest_get")
            .name("REST API (GET)")
            .description("Generic GET request")
            .urlPattern("https://api.example.com/endpoint")
            .method(EnumHttpMethod.GET)
            .authType(EnumAuthType.NONE)
            .build());

        // Generic REST API POST
        register(RequestTemplate.builder("rest_post")
            .name("REST API (POST)")
            .description("Generic POST request with JSON")
            .urlPattern("https://api.example.com/endpoint")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.BEARER)
            .parameter("key", "value")
            .build());

        // ntfy Notification
        register(RequestTemplate.builder("ntfy")
            .name("ntfy Notification")
            .description("Send push notification via ntfy")
            .urlPattern("https://ntfy.sh/{topic}")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.NONE)
            .parameter("message", "Notification from Minecraft!")
            .parameter("title", "Minecraft Event")
            .build());

        // Pushover
        register(RequestTemplate.builder("pushover")
            .name("Pushover")
            .description("Send Pushover notification")
            .urlPattern("https://api.pushover.net/1/messages.json")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.NONE)
            .parameter("token", "{app_token}")
            .parameter("user", "{user_key}")
            .parameter("message", "Notification from Minecraft!")
            .build());

        // Node-RED HTTP Request
        register(RequestTemplate.builder("nodered")
            .name("Node-RED")
            .description("Trigger Node-RED flow")
            .urlPattern("http://{host}:1880/{endpoint}")
            .method(EnumHttpMethod.POST)
            .authType(EnumAuthType.NONE)
            .parameter("event", "minecraft")
            .build());
    }

    /**
     * Register a template.
     */
    public static void register(RequestTemplate template) {
        TEMPLATES.put(template.getId(), template);
    }

    /**
     * Get a template by ID.
     */
    public static Optional<RequestTemplate> get(String id) {
        return Optional.ofNullable(TEMPLATES.get(id));
    }

    /**
     * Get all registered templates.
     */
    public static List<RequestTemplate> getAll() {
        return new ArrayList<>(TEMPLATES.values());
    }

    /**
     * Get template names for display.
     */
    public static List<String> getTemplateNames() {
        List<String> names = new ArrayList<>();
        names.add("None"); // First option is no template
        for (RequestTemplate template : TEMPLATES.values()) {
            names.add(template.getName());
        }
        return names;
    }

    /**
     * Get template by display name.
     */
    public static Optional<RequestTemplate> getByName(String name) {
        for (RequestTemplate template : TEMPLATES.values()) {
            if (template.getName().equals(name)) {
                return Optional.of(template);
            }
        }
        return Optional.empty();
    }

    /**
     * Get number of registered templates.
     */
    public static int size() {
        return TEMPLATES.size();
    }
}
