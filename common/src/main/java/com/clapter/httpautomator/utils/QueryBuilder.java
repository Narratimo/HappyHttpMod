package com.clapter.httpautomator.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.StringJoiner;

public class QueryBuilder {

    public static String paramsToQueryString(Map<String, String> map){
        try {
            StringJoiner queryString = new StringJoiner("&");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String encodedKey = null;

                encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");

                String encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
                queryString.add(encodedKey + "=" + encodedValue);
            }
            return queryString.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
