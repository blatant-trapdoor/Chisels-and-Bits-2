package nl.dgoossens.chiselsandbits2.common.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

import java.util.function.Supplier;

/**
 * Starts/ends an undo group.
 * Sent SERVER -> CLIENT.
 */
public class SGroupMethod {
    public static class BeginGroup {
        public BeginGroup() {}

        public static void encode(BeginGroup msg, PacketBuffer buf) { }

        public static BeginGroup decode(PacketBuffer buffer) {
            return new BeginGroup();
        }

        public static void handle(final BeginGroup pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                    ChiselsAndBits2.getInstance().getClient().getUndoTracker().beginGroup(Minecraft.getInstance().player)
            );
            ctx.get().setPacketHandled(true);
        }
    }

    public static class EndGroup {
        public EndGroup() {}

        public static void encode(EndGroup msg, PacketBuffer buf) {}

        public static EndGroup decode(PacketBuffer buffer) {
            return new EndGroup();
        }

        public static void handle(final EndGroup pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                ChiselsAndBits2.getInstance().getClient().getUndoTracker().endGroup(Minecraft.getInstance().player)
            );
            ctx.get().setPacketHandled(true);
        }
    }
}
