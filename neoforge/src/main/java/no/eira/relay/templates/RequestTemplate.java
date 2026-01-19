package no.eira.relay.templates;

import no.eira.relay.enums.EnumAuthType;
import no.eira.relay.enums.EnumHttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Pre-configured template for common HTTP request patterns.
 */
public class RequestTemplate {

    private final String id;
    private final String name;
    private final String description;
    private final String urlPattern;
    private final EnumHttpMethod method;
    private final EnumAuthType authType;
    private final Map<String, String> defaultParameters;
    private final String contentType;

    private RequestTemplate(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.urlPattern = builder.urlPattern;
        this.method = builder.method;
        this.authType = builder.authType;
        this.defaultParameters = new HashMap<>(builder.defaultParameters);
        this.contentType = builder.contentType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public EnumHttpMethod getMethod() {
        return method;
    }

    public EnumAuthType getAuthType() {
        return authType;
    }

    public Map<String, String> getDefaultParameters() {
        return new HashMap<>(defaultParameters);
    }

    public String getContentType() {
        return contentType;
    }

    /**
     * Check if the URL pattern contains placeholders that need user input.
     */
    public boolean hasPlaceholders() {
        return urlPattern.contains("{") && urlPattern.contains("}");
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static class Builder {
        private final String id;
        private String name = "";
        private String description = "";
        private String urlPattern = "";
        private EnumHttpMethod method = EnumHttpMethod.POST;
        private EnumAuthType authType = EnumAuthType.NONE;
        private final Map<String, String> defaultParameters = new HashMap<>();
        private String contentType = "application/json";

        public Builder(String id) {
            this.id = id;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder urlPattern(String urlPattern) {
            this.urlPattern = urlPattern;
            return this;
        }

        public Builder method(EnumHttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder authType(EnumAuthType authType) {
            this.authType = authType;
            return this;
        }

        public Builder parameter(String key, String value) {
            this.defaultParameters.put(key, value);
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public RequestTemplate build() {
            return new RequestTemplate(this);
        }
    }
}
