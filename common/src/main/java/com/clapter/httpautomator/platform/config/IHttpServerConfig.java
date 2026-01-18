package com.clapter.httpautomator.platform.config;

import java.util.List;

public interface IHttpServerConfig {

    int getPort();
    String getLocalAdress();
    List<GlobalParam> getGlobalParams();
    String getGlobalRedirect();

}
