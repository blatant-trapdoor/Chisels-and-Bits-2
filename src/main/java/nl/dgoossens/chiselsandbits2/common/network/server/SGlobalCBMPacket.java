package nl.dgoossens.chiselsandbits2.common.network.server;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.client.util.ClientItemPropertyUtil;
import nl.dgoossens.chiselsandbits2.common.impl.item.ItemMode;
import nl.dgoossens.chiselsandbits2.common.network.IPacket;

import java.util.function.Function;
import java.util.function.Supplier;

public class SGlobalCBMPacket implements IPacket<SGlobalCBMPacket> {
    private ItemMode mode;

    public SGlobalCBMPacket() {}
    public SGlobalCBMPacket(final ItemMode mode) {
        this.mode = mode;
    }

    public ItemMode readCBM() {
        return mode;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(mode.ordinal());
    }

    @Override
    public Function<PacketBuffer, SGlobalCBMPacket> getDecoder() {
        return (buffer) -> new SGlobalCBMPacket(ItemMode.values()[buffer.readVarInt()]);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            ClientItemPropertyUtil.readGlobalCBM(this)
        );
        ctx.get().setPacketHandled(true);
    }
}
