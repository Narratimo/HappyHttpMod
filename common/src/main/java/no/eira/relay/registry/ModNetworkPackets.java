package no.eira.relay.registry;

import no.eira.relay.network.PacketDirection;
import no.eira.relay.network.packet.CHttpSenderOpenGuiPacket;
import no.eira.relay.network.packet.CSyncHttpReceiverValuesPacket;
import no.eira.relay.network.packet.SUpdateHttpReceiverValuesPacket;
import no.eira.relay.network.packet.SUpdateHttpSenderValuesPacket;
import no.eira.relay.platform.Services;

public class ModNetworkPackets {

    public static void registerPackets(){
        // HTTP Receiver packets
        Services.PACKET_HANDLER.registerPacket(SUpdateHttpReceiverValuesPacket.class,
                SUpdateHttpReceiverValuesPacket::encode, SUpdateHttpReceiverValuesPacket::new, PacketDirection.PLAY_TO_SERVER);
        Services.PACKET_HANDLER.registerPacket(CSyncHttpReceiverValuesPacket.class,
                CSyncHttpReceiverValuesPacket::encode, CSyncHttpReceiverValuesPacket::new, PacketDirection.PLAY_TO_CLIENT);

        // HTTP Sender packets
        Services.PACKET_HANDLER.registerPacket(SUpdateHttpSenderValuesPacket.class,
                SUpdateHttpSenderValuesPacket::encode, SUpdateHttpSenderValuesPacket::new, PacketDirection.PLAY_TO_SERVER);
        Services.PACKET_HANDLER.registerPacket(CHttpSenderOpenGuiPacket.class,
                CHttpSenderOpenGuiPacket::encode, CHttpSenderOpenGuiPacket::new, PacketDirection.PLAY_TO_CLIENT);
    }

}
