package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;

import java.util.function.Supplier;

/**
 * Executes the undo action.
 * Sent CLIENT -> SERVER.
 */
public class CUndoPacket {
    private BlockPos pos;
    private VoxelBlobStateReference before, after;

    private CUndoPacket() {}

    public CUndoPacket(final BlockPos pos, final VoxelBlobStateReference before, final VoxelBlobStateReference after) {
        this.pos = pos;
        this.after = after;
        this.before = before;
    }

    public boolean handle(final PlayerEntity player) {
        return false;
    }

    public static void encode(CUndoPacket msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.pos);
        final byte[] bef = msg.before.getByteArray();
        buf.writeVarInt(bef.length);
        buf.writeBytes(bef);
        final byte[] aft = msg.after.getByteArray();
        buf.writeVarInt(aft.length);
        buf.writeBytes(aft);
    }

    public static CUndoPacket decode(PacketBuffer buffer) {
        CUndoPacket pc = new CUndoPacket();
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

    public static void handle(final CUndoPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                pkt.handle(ctx.get().getSender())
        );
        ctx.get().setPacketHandled(true);
    }
}
