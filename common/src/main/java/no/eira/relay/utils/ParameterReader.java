package no.eira.relay.utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ParameterReader {

    public static Map<String, String> getAllParameters(HttpExchange exchange) throws IOException {
        Map<String, String> parameters = new HashMap<>();

        // Get POST parameters
        if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            int b;
            StringBuilder buf = new StringBuilder(512);
            while ((b = br.read()) != -1) {
                buf.append((char) b);
            }
            String requestBody = buf.toString();
            br.close();
            isr.close();
            parameters.putAll(parseRequestBody(requestBody));
        }

        // Get GET parameters
        if ("get".equalsIgnoreCase(exchange.getRequestMethod())) {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = "";
                    if (keyValue.length > 1) {
                        value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    }
                    parameters.put(key, value);
                }
            }
        }

        return parameters;
    }

    private static Map<String, String> parseRequestBody(String requestBody) {
        return JsonUtils.getParametersAsMap(requestBody);
    }
}
