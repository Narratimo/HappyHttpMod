package no.eira.relay.variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import no.eira.relay.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for extracting values from JSON using simple path expressions.
 *
 * Supports paths like:
 * - "field" - top-level field
 * - "data.value" - nested object access
 * - "items[0]" - array index access
 * - "data.items[0].name" - combined access
 */
public class JsonPathExtractor {

    // Pattern for array access: fieldName[index]
    private static final Pattern ARRAY_PATTERN = Pattern.compile("^(.+?)\\[(\\d+)]$");

    /**
     * Extract a value from JSON using a dot-notation path.
     *
     * @param json The JSON string to extract from
     * @param path The path expression (e.g., "data.user.name" or "items[0].id")
     * @return The extracted value as a string, or empty string if not found
     */
    public static String extract(String json, String path) {
        return extract(json, path, "");
    }

    /**
     * Extract a value from JSON using a dot-notation path.
     *
     * @param json The JSON string to extract from
     * @param path The path expression
     * @param defaultValue Value to return if path not found
     * @return The extracted value as a string, or defaultValue if not found
     */
    public static String extract(String json, String path, String defaultValue) {
        if (json == null || json.isEmpty() || path == null || path.isEmpty()) {
            return defaultValue;
        }

        try {
            JsonElement root = JsonParser.parseString(json);
            JsonElement result = navigatePath(root, path);

            if (result == null || result.isJsonNull()) {
                return defaultValue;
            }

            return elementToString(result);
        } catch (Exception e) {
            Constants.LOG.debug("Failed to extract path '{}' from JSON: {}", path, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Check if a path exists in the JSON.
     */
    public static boolean pathExists(String json, String path) {
        if (json == null || json.isEmpty() || path == null || path.isEmpty()) {
            return false;
        }

        try {
            JsonElement root = JsonParser.parseString(json);
            JsonElement result = navigatePath(root, path);
            return result != null && !result.isJsonNull();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Navigate through JSON using a path expression.
     */
    private static JsonElement navigatePath(JsonElement current, String path) {
        if (current == null || path == null || path.isEmpty()) {
            return current;
        }

        // Handle path starting with $ (JSONPath style) - just strip it
        if (path.startsWith("$.")) {
            path = path.substring(2);
        } else if (path.startsWith("$")) {
            path = path.substring(1);
        }

        String[] segments = path.split("\\.");

        for (String segment : segments) {
            if (current == null || current.isJsonNull()) {
                return null;
            }

            // Check for array access
            Matcher arrayMatcher = ARRAY_PATTERN.matcher(segment);
            if (arrayMatcher.matches()) {
                String fieldName = arrayMatcher.group(1);
                int index = Integer.parseInt(arrayMatcher.group(2));

                // Navigate to field first if not empty
                if (!fieldName.isEmpty()) {
                    if (!current.isJsonObject()) {
                        return null;
                    }
                    current = current.getAsJsonObject().get(fieldName);
                }

                // Then access array index
                if (current == null || !current.isJsonArray()) {
                    return null;
                }
                JsonArray array = current.getAsJsonArray();
                if (index < 0 || index >= array.size()) {
                    return null;
                }
                current = array.get(index);
            } else {
                // Simple field access
                if (!current.isJsonObject()) {
                    return null;
                }
                current = current.getAsJsonObject().get(segment);
            }
        }

        return current;
    }

    /**
     * Convert a JsonElement to a string representation.
     */
    private static String elementToString(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }

        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                return primitive.getAsString();
            } else if (primitive.isNumber()) {
                // Return number without unnecessary decimal places
                Number num = primitive.getAsNumber();
                if (num.doubleValue() == num.longValue()) {
                    return String.valueOf(num.longValue());
                }
                return num.toString();
            } else if (primitive.isBoolean()) {
                return String.valueOf(primitive.getAsBoolean());
            }
        }

        // For arrays and objects, return JSON string
        return element.toString();
    }

    /**
     * Extract an integer value from JSON.
     */
    public static int extractInt(String json, String path, int defaultValue) {
        String value = extract(json, path, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(value);
            } catch (NumberFormatException e2) {
                return defaultValue;
            }
        }
    }

    /**
     * Extract a double value from JSON.
     */
    public static double extractDouble(String json, String path, double defaultValue) {
        String value = extract(json, path, null);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Extract a boolean value from JSON.
     */
    public static boolean extractBoolean(String json, String path, boolean defaultValue) {
        String value = extract(json, path, null);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value) || "1".equals(value);
    }
}
