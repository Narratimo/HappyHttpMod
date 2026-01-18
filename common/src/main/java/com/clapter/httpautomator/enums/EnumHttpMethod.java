package com.clapter.httpautomator.enums;

import net.minecraft.network.chat.Component;

public enum EnumHttpMethod {

    GET("GET"),
    POST("POST");

    private final String method;

    EnumHttpMethod(String method) {
        this.method = method;
    }

    public Component getComponent(){
        return Component.literal(this.method);
    }

    public static EnumHttpMethod getByName(String name){
        for(EnumHttpMethod httpMethod : values()){
            if(httpMethod.method.equals(name)){
                return httpMethod;
            }
        }
        return null;
    }

}
