package nl.dgoossens.chiselsandbits2.common.network.server;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitAccess;
import nl.dgoossens.chiselsandbits2.api.BitOperation;
import nl.dgoossens.chiselsandbits2.client.UndoTracker;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.utils.InventoryUtils;

import java.util.*;
import java.util.function.Supplier;

/**
 * Adds an undo step to the client tracker.
 * Sent SERVER -> CLIENT.
 */
public class SAddUndoStep {
    private BlockPos pos;
    private VoxelBlobStateReference before, after;

    private SAddUndoStep() {}

    public SAddUndoStep(final BlockPos pos, final VoxelBlobStateReference before, final VoxelBlobStateReference after) {
        this.pos = pos;
        this.after = after;
        this.before = before;
    }

    public static void encode(SAddUndoStep msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.pos);
        final byte[] bef = msg.before.getByteArray();
        buf.writeVarInt(bef.length);
        buf.writeBytes(bef);
        final byte[] aft = msg.after.getByteArray();
        buf.writeVarInt(aft.length);
        buf.writeBytes(aft);
    }

    public static SAddUndoStep decode(PacketBuffer buffer) {
        SAddUndoStep pc = new SAddUndoStep();
        pc.pos = buffer.readBlockPos();
        final int lena = buffer.readVarInt();
        final byte[] ta = new byte[lena];
        buffer.readBytes(ta);
        final int lenb = buffer.readVarInt();
        final byte[] tb = new byte[lenb];
        buffer.readBytes(tb);

        pc.before = new VoxelBlobStateReference(ta);
        pc.after = new VoxelBlobStateReference(tb);
        return pc;
    }

    public static void handle(final SAddUndoStep pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            ChiselsAndBits2.getInstance().getClient().getUndoTracker().add(Minecraft.getInstance().player, Minecraft.getInstance().player.getEntityWorld(), pkt.pos, pkt.before, pkt.after)
        );
        ctx.get().setPacketHandled(true);
    }
}
