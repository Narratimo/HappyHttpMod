package com.clapter.httpautomator.http;

import com.clapter.httpautomator.Constants;
import com.clapter.httpautomator.http.api.IHttpHandler;
import com.clapter.httpautomator.http.api.IHttpServer;
import com.clapter.httpautomator.platform.Services;
import com.clapter.httpautomator.utils.ImplLoader;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.*;

public class HttpServerImpl implements IHttpServer {

    private HttpServer server;
    private String localAdress;
    private String externalAdress;
    //private Map<Integer, IHttpHandler> handlerMap;
    private Map<String, IHttpHandler> handlerMap;
    //USING MAP INSTEAD OF QUEUE FOR FAST RETRIEVAL OF A HANDLER FROM THE QUEUE BY ITS KEY
    private Map<String, IHttpHandler> handlerToRegisterQueue;


    public HttpServerImpl(){
        handlerMap = new HashMap<String, IHttpHandler>();
        handlerToRegisterQueue = new HashMap<String, IHttpHandler>();
    }

    public boolean startServer() throws IOException {
        InetSocketAddress sockAdress = new InetSocketAddress(Services.HTTP_CONFIG.getPort());
        server = HttpServer.create(sockAdress, 0);
        server.setExecutor(null); // creates a default executor
        server.start();
        this.handleHandlersInQueue();
        Constants.LOG.info("HTTP SERVER STARTED");
        try {
            this.localAdress = getInternalIPv4Address();
            this.externalAdress = getExternalIPAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static String getInternalIPv4Address() throws Exception {
        if(Services.HTTP_CONFIG.getLocalAdress().isEmpty()) {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof java.net.Inet4Address) {

                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        }
        return Services.HTTP_CONFIG.getLocalAdress();
    }

    private static String getExternalIPAddress() {
        String externalIP = "";
        String serviceURL = "http://checkip.amazonaws.com"; // This service returns the public IP address of the requester

        try {
            URL url = new URL(serviceURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                externalIP = in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return externalIP;
    }

    private void handleHandlersInQueue() {
        handlerToRegisterQueue.values().forEach(this::registerHandler);
        handlerToRegisterQueue.clear();
    }

    @Override
    public void initHandlers(){
        List<IHttpHandler> handlerList = ImplLoader.loadAll(IHttpHandler.class);
        handlerList.forEach(this::registerHandler);
    }

    @Override
    public void registerHandler(IHttpHandler handler) {
        if(server == null){
            handlerToRegisterQueue.put(handler.getUrl(), handler);
        }else{
            this.handleRegisteringHandlers(handler);
        }
    }

    @Override
    public IHttpHandler getHandlerByUrl(String url) {
        if(handlerMap.get(url) != null)return handlerMap.get(url);
        if(handlerToRegisterQueue.get(url) != null)return handlerToRegisterQueue.get(url);
        return null;
    }

    @Override
    public void removeHandler(IHttpHandler handler) {
        try {

            handlerMap.remove(handler.getUrl());
            server.removeContext(handler.getUrl());

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getServerAddress() {
        if(this.server == null)return "";
        return this.localAdress+":"+this.server.getAddress().getPort();
    }

    @Override
    public String getServerPublicAdress() {
        if(this.server == null)return "";
        return this.externalAdress+":"+this.server.getAddress().getPort();
    }

    private void handleRegisteringHandlers(IHttpHandler handler) {
        if(!handlerMap.containsKey(handler.getUrl())) {
            registerAndPutInMap(handler);
        }else{
            //ALREADY CONTAINING A HANDLER FOR THAT ID (FOR THAT BLOCK IN OUR TEST)
            //FOR NOW JUST OVERRIDE WITH NEW ONE
            //server.removeContext(handler.getUrl());
            registerAndPutInMap(handler);

        }
    }

    private void registerAndPutInMap(IHttpHandler handler) {
        //String url = this.validateUrl(handler.getUrl());
        server.createContext(handler.getUrl(), handler);
        handlerMap.put(handler.getUrl(), handler);
    }


    @Override
    public void stopServer() {
        if(server != null){
            server.stop(0);
            server = null;
        }
    }


}
