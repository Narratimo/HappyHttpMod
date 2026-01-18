package com.clapter.httpautomator.registry;

import com.clapter.httpautomator.network.PacketDirection;
import com.clapter.httpautomator.network.packet.CSyncHttpReceiverValuesPacket;
import com.clapter.httpautomator.network.packet.SUpdateHttpReceiverValuesPacket;
import com.clapter.httpautomator.platform.Services;

public class ModNetworkPackets {

    public static void registerPackets(){
        Services.PACKET_HANDLER.registerPacket(SUpdateHttpReceiverValuesPacket.class,
                SUpdateHttpReceiverValuesPacket::encode, SUpdateHttpReceiverValuesPacket::new, PacketDirection.PLAY_TO_SERVER);
        Services.PACKET_HANDLER.registerPacket(CSyncHttpReceiverValuesPacket.class,
                CSyncHttpReceiverValuesPacket::encode, CSyncHttpReceiverValuesPacket::new, PacketDirection.PLAY_TO_CLIENT);
    }

}
