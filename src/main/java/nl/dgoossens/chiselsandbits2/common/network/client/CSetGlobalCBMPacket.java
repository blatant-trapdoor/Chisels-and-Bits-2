package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.common.impl.item.GlobalCBMCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.impl.item.ItemMode;
import nl.dgoossens.chiselsandbits2.common.network.IPacket;

import java.util.function.Function;
import java.util.function.Supplier;

public class CSetGlobalCBMPacket implements IPacket<CSetGlobalCBMPacket> {
    private ItemMode mode;

    public CSetGlobalCBMPacket() {}
    public CSetGlobalCBMPacket(final ItemMode mode) {
        this.mode = mode;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarInt(mode.ordinal());
    }

    @Override
    public Function<PacketBuffer, CSetGlobalCBMPacket> getDecoder() {
        return (buffer) -> new CSetGlobalCBMPacket(ItemMode.values()[buffer.readVarInt()]);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            //Set the sending player's capability
            ctx.get().getSender().getCapability(GlobalCBMCapabilityProvider.GLOBAL_CBM).ifPresent(cap -> {
                cap.insert(mode);
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
