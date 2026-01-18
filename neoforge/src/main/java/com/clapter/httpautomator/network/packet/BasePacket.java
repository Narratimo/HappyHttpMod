package com.clapter.httpautomator.network.packet;

import com.clapter.httpautomator.network.IPacketContext;
import net.minecraft.network.FriendlyByteBuf;

public abstract class BasePacket {

    public abstract void handle(IPacketContext context);

}
