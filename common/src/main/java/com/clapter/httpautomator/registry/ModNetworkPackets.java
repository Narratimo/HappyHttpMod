package com.clapter.httpautomator.registry;

import com.clapter.httpautomator.network.PacketDirection;
import com.clapter.httpautomator.network.packet.CHttpReceiverOpenGuiPacket;
import com.clapter.httpautomator.network.packet.CHttpSenderOpenGuiPacket;
import com.clapter.httpautomator.network.packet.SUpdateHttpReceiverValuesPacket;
import com.clapter.httpautomator.network.packet.SUpdateHttpSenderValuesPacket;
import com.clapter.httpautomator.platform.Services;

public class ModNetworkPackets {

    public static void registerPackets(){
        Services.PACKET_HANDLER.registerPacket(SUpdateHttpReceiverValuesPacket.class,
                SUpdateHttpReceiverValuesPacket::encode, SUpdateHttpReceiverValuesPacket::new, PacketDirection.PLAY_TO_SERVER);

        Services.PACKET_HANDLER.registerPacket(SUpdateHttpSenderValuesPacket.class,
                SUpdateHttpSenderValuesPacket::encode, SUpdateHttpSenderValuesPacket::new, PacketDirection.PLAY_TO_SERVER);

        Services.PACKET_HANDLER.registerPacket(CHttpReceiverOpenGuiPacket.class,
                CHttpReceiverOpenGuiPacket::encode, CHttpReceiverOpenGuiPacket::new, PacketDirection.PLAY_TO_CLIENT);

        Services.PACKET_HANDLER.registerPacket(CHttpSenderOpenGuiPacket.class,
                CHttpSenderOpenGuiPacket::encode, CHttpSenderOpenGuiPacket::new, PacketDirection.PLAY_TO_CLIENT);

    }

}
