package com.clapter.httpautomator.platform.network;

import com.clapter.httpautomator.network.packet.BasePacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

public class PacketServerHandler {

    //THE HANDLER METHOD FOR HANDLING PACKETS AFTER THEIR DECODING PROCESS
    //HAS TO START HERE, SINCE THE CONTEXT OBJECT IS A FORGE CLASS
    //SERVER HAS TO HAVE EXTERNAL HANDLER WITH unsafeRun
    //SOURCE: https://docs.minecraftforge.net/en/latest/networking/simpleimpl/
    public static void handle(BasePacket packet, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            if (context.getDirection().getReceptionSide().isClient()) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> packet.handle(new PacketContext(context)));
            }

        });
        context.setPacketHandled(true);
    }

}
