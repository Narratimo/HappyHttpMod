package com.clapter.httpautomator.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    public static Map<String, String> getParametersAsMap(String requestBody) {
        Map<String, String> parameters = new HashMap<>();

        JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString())
                continue; // Only process string values
            parameters.put(entry.getKey(), value.getAsString());
        }
        return parameters;
    }

    public static String parametersFromMapToString(Map<String, String> parameterMap){
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
            jsonObject.addProperty(entry.getKey(), entry.getValue());
        }
        Gson gson = new Gson();
        return gson.toJson(jsonObject);
    }

}
