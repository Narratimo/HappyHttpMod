package no.eira.relay;

import no.eira.relay.http.HttpClientImpl;
import no.eira.relay.http.HttpServerImpl;
import no.eira.relay.http.api.IHttpClient;
import no.eira.relay.http.api.IHttpServer;
import no.eira.relay.registry.ModBlockEntities;
import no.eira.relay.registry.ModBlocks;
import no.eira.relay.registry.ModItems;
import no.eira.relay.registry.ModNetworkPackets;

import java.io.IOException;

public class CommonClass {

    public static final IHttpServer HTTP_SERVER = new HttpServerImpl();
    public static final IHttpClient HTTP_CLIENT = new HttpClientImpl();

    public static void init() {
        ModBlocks.registerBlocks();
        ModBlockEntities.registerBlockEntities();
        ModItems.registerItems();
    }

    public static void registerPackets(){
        ModNetworkPackets.registerPackets();
    }

    //On Server Starting Callback. Is used for starting the HTTP-Server
    public static void onServerStarting(){
        try {
            if(HTTP_SERVER.startServer()){
                Constants.LOG.info("HTTP Server started on: ");
            }
        }catch (IOException e){
            //SOMETHING WENT WRONG
            e.printStackTrace();
        }
        catch (Exception e){
            //e.printStackTrace();
        }
    }

    public static void onServerStarted(){
        HTTP_SERVER.initHandlers();
    }

    public static void onServerStopping(){
        HTTP_SERVER.stopServer();
    }


}