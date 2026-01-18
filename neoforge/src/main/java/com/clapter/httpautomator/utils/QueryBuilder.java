package com.clapter.httpautomator.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

public class QueryBuilder {

    public static String paramsToQueryString(Map<String, String> map) {
        StringJoiner queryString = new StringJoiner("&");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
            queryString.add(encodedKey + "=" + encodedValue);
        }
        return queryString.toString();
    }
}
