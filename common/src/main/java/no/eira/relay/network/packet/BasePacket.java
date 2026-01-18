package no.eira.relay.network.packet;

import no.eira.relay.network.IPacketContext;
import net.minecraft.network.FriendlyByteBuf;

public abstract class BasePacket {

    public abstract void handle(IPacketContext context);

}
