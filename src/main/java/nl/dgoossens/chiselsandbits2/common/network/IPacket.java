package nl.dgoossens.chiselsandbits2.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A generic interface for packets.
 */
public interface IPacket<T extends IPacket<T>> {
    /**
     * Write this packet to a buffer.
     */
    void encode(PacketBuffer buf);

    /**
     * Get the function that decodes this packet from a buffer.
     */
    Function<PacketBuffer, T> getDecoder();

    /**
     * Handles receiving this packet.
     */
    void handle(Supplier<NetworkEvent.Context> ctx);
}
