package no.eira.relay;

import net.minecraft.server.level.ServerLevel;
import no.eira.relay.http.HttpClientImpl;
import no.eira.relay.http.HttpServerImpl;
import no.eira.relay.http.api.IHttpClient;
import no.eira.relay.http.api.IHttpServer;
import no.eira.relay.http.handlers.BroadcastHandler;
import no.eira.relay.http.handlers.RedstoneHandler;
import no.eira.relay.registry.ModBlockEntities;
import no.eira.relay.registry.ModBlocks;
import no.eira.relay.registry.ModItems;
import no.eira.relay.registry.ModNetworkPackets;
import no.eira.relay.variables.VariableStorage;

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
        // Clear all response variables
        VariableStorage.getInstance().clearAll();
    }

    /**
     * Initialize the server level for HTTP handlers that need world access
     */
    public static void initServerLevel(ServerLevel level) {
        RedstoneHandler.setServerLevel(level);
        BroadcastHandler.setServerLevel(level);
        Constants.LOG.info("Server level initialized for HTTP handlers");
    }

    /**
     * Called every server tick to process redstone emissions
     */
    public static void onServerTick() {
        RedstoneHandler.tick();
    }
}