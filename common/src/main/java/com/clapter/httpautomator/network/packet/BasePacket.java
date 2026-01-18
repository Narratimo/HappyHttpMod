package com.clapter.httpautomator.network.packet;

import com.clapter.httpautomator.network.IPacketContext;

public abstract class BasePacket {

    public abstract void handle(IPacketContext context);

}
