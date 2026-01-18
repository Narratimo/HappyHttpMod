package com.clapter.httpautomator.http.handlers;

import com.clapter.httpautomator.CommonClass;
import com.clapter.httpautomator.blockentity.HttpReceiverBlockEntity;
import com.clapter.httpautomator.http.api.IHttpHandler;
import com.clapter.httpautomator.platform.Services;
import com.clapter.httpautomator.platform.config.GlobalParam;
import com.clapter.httpautomator.utils.JsonUtils;
import com.clapter.httpautomator.utils.ParameterReader;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

public class HttpReceiverBlockHandler implements IHttpHandler {

    //MAYBE SWITCH TO SET FOR FASTER REMOVAL OF ENTRIES
    private List<HttpReceiverBlockEntity> entityList;
    private String url;
    private static final String ALLOWED_METHOD = "POST";

    public HttpReceiverBlockHandler(HttpReceiverBlockEntity entity, String url){
        this.entityList = new ArrayList<HttpReceiverBlockEntity>(Collections.singletonList(entity));
        this.url = url;
    }

    public void removeBlockFromHandler(HttpReceiverBlockEntity block){
        //System.out.println("REMOVING BLOCK FROM "+this);
        //System.out.println(this.entityList);
        this.entityList.remove(block);
        if(this.entityList.isEmpty()){
            //System.out.println("REMOVING HANDLER "+this);
            CommonClass.HTTP_SERVER.removeHandler(this);
        }
    }

    public static HttpReceiverBlockHandler create(HttpReceiverBlockEntity entity, String url){
        String validatedUrl = validateUrl(url);
        IHttpHandler handler = CommonClass.HTTP_SERVER.getHandlerByUrl(validatedUrl);
        //System.out.println("FOUND HANDLER: "+handler);
        if(handler != null) {
            if (handler instanceof HttpReceiverBlockHandler receiverHandler) {
                //ADD TO EXISTING HADNLER
                receiverHandler.addBlockEntity(entity);
                return receiverHandler;
            }
            //ERROR BECAUSE URL ALREADY EXISTS AND IS NOT A RECEIVER HANDLER
            return null;
        }
        HttpReceiverBlockHandler newHandler = new HttpReceiverBlockHandler(entity, validatedUrl);
        CommonClass.HTTP_SERVER.registerHandler(newHandler);
        return newHandler;
    }

    private static String validateUrl(String url){
        if(!url.startsWith("/")) return "/"+url;
        return url;
    }

    private void addBlockEntity(HttpReceiverBlockEntity entity){
        this.entityList.add(entity);
    }

    private void removeBlockEntity(HttpReceiverBlockEntity entity){
        this.entityList.remove(entity);
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public List<String> httpMethods() {
        return List.of(ALLOWED_METHOD);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        List<GlobalParam> globalParams = Services.HTTP_CONFIG.getGlobalParams();
        if(!globalParams.isEmpty()){
            if(!this.checkGlobalParams(exchange, globalParams)){
                this.writeResponse(exchange);
                return;
            }
        }
        entityList.forEach((blockEntity) -> {
            if(this.shouldEntityBePowered(exchange, blockEntity)){
                blockEntity.onSignal();
            }
        });
        this.writeResponse(exchange);
    }

    private boolean checkGlobalParams(HttpExchange exchange, List<GlobalParam> globalParams) throws IOException {
        //CHECK REQUEST PARAMS WITH EVERY GLOBAL PARAM
        Map<String, String> params = ParameterReader.getAllParameters(exchange);
        String redirect = Services.HTTP_CONFIG.getGlobalRedirect();
        System.out.println("HERE "+redirect);
        if(params.isEmpty()){
            //GLOBAL REDIRECT
            //String redirect = Services.HTTP_CONFIG.getGlobalRedirect();
            //System.out.println(redirect);
            if(redirect != null && !redirect.isEmpty()) {
                exchange.getResponseHeaders().add("Location", redirect);
                exchange.sendResponseHeaders(308, 0);
            }
            return false;
        }
        for(GlobalParam param : globalParams){
            String requestParam = params.get(param.name);
            if(requestParam == null){
                //PARAM NOT IN THE REQUEST
                if(param.redirectWrong != null && !param.redirectWrong.isEmpty()) {
                    exchange.getResponseHeaders().add("Location", param.redirectWrong);
                    exchange.sendResponseHeaders(308, 0);
                }
                return false;
            }
            if(!param.value.equals(requestParam)){
                if(param.redirectWrong != null && !param.redirectWrong.isEmpty()) {
                    exchange.getResponseHeaders().add("Location", param.redirectWrong);
                    exchange.sendResponseHeaders(308, 0);
                }
                return false;
            }
        }
        return true;
    }

    private boolean shouldEntityBePowered(HttpExchange exchange, HttpReceiverBlockEntity entity){
        try {
            Map<String, String> params = ParameterReader.getAllParameters(exchange);
            if(params.isEmpty()){
                return entity.getValues().parameterMap.isEmpty();
            }
            return this.allParametersValidForEntity(params, entity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean allParametersValidForEntity(Map<String, String> params, HttpReceiverBlockEntity entity) {
        for(Map.Entry<String, String> entry : entity.getValues().parameterMap.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if(!params.containsKey(key)) return false;
            else if(!params.get(key).equals(value)) return false;
        }
        return true;
    }

    private void writeResponse(HttpExchange exchange) throws IOException {
        String redirect = this.getRedirect(exchange);
        if(redirect != null){
            exchange.getResponseHeaders().add("Location", redirect);
            exchange.getResponseHeaders().add("cache-control", "no-store, max-age=0");
            exchange.sendResponseHeaders(308, 0);
        }else{
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        exchange.close();
    }

    //REDIRECTS CLIENT, IF ONE OF THE BLOCKS HAS A REDIRECTION SETTING
    //IN CASE OF MANY BLOCKS HAVING SAME ENDPOINT URL, IT TAKES THE LAST FOUND REDIRECT
    //URL
    private String getRedirect(HttpExchange exchange) throws IOException {
        String redirectUrl = null;
        for(HttpReceiverBlockEntity entity : this.entityList){
            if(entity.getValues().redirectClientUrl != null &&
                    !entity.getValues().redirectClientUrl.isEmpty()){
                redirectUrl = entity.getValues().redirectClientUrl;
            }
        }
        return redirectUrl;
    }

    @Override
    public String toString() {
        return "Handler: "+this.url;
    }
}
