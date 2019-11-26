package nl.dgoossens.chiselsandbits2.common.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.ter.RenderCache;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

import java.util.*;
import java.util.function.Supplier;

/**
 * Force a chunk to be rerendered.
 * Sent SERVER -> CLIENT.
 */
public class SRebuildChunkPacket {
    private BlockPos pos;

    public SRebuildChunkPacket() {}
    public SRebuildChunkPacket(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(SRebuildChunkPacket msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static SRebuildChunkPacket decode(PacketBuffer buffer) {
        SRebuildChunkPacket c = new SRebuildChunkPacket();
        c.pos = buffer.readBlockPos();
        return c;
    }

    public static void handle(final SRebuildChunkPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                Optional.ofNullable(ChiseledBlockTileEntity.findRenderChunk(pkt.pos, Minecraft.getInstance().player.getEntityWorld(), () -> null))
                    .ifPresent(RenderCache::requestImmediateRebuild)
        );
        ctx.get().setPacketHandled(true);
    }
}
