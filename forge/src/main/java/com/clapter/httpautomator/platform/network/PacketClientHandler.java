package com.clapter.httpautomator.platform.network;

import com.clapter.httpautomator.network.packet.BasePacket;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class PacketClientHandler {

    //THE HANDLER METHOD FOR HANDLING PACKETS AFTER THEIR DECODING PROCESS
    //HAS TO START HERE, SINCE THE CONTEXT OBJECT IS A FORGE CLASS
    public static void handle(BasePacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> packet.handle(new PacketContext(context)));
        context.setPacketHandled(true);
    }

}
