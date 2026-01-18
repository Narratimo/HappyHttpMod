package com.clapter.httpautomator.platform.config;

public class GlobalParam {

    public final String index;
    public String name;
    public String value;
    public String redirectWrong;

    public GlobalParam(String index, String name, String value, String redirectWrong) {
        this.index = index;
        this.name = name;
        this.value = value;
        this.redirectWrong = redirectWrong;

    }

    public GlobalParam(String paramIndex) {
        this.index = paramIndex;
    }
}
