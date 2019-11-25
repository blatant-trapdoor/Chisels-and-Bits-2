package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitAccess;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;

import java.util.*;
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

    public boolean handle(final PlayerEntity player, boolean applyChanges) {
        if(!inRange(player)) {
            player.sendStatusMessage(new TranslationTextComponent("general."+ ChiselsAndBits2.MOD_ID+".info.out_of_range"), true);
            return false;
        }
        try {
            final World world = player.getEntityWorld();
            final Optional<BitAccess> baOpt = ChiselsAndBits2.getInstance().getAPI().getBitAccess(world, pos);
            if(baOpt.isPresent()) {
                final BitAccess ba = baOpt.get();
                final VoxelBlob bbef = before.getVoxelBlob();
                final VoxelBlob baft = after.getVoxelBlob();

                final VoxelBlob target = ba.getNativeBlob();

                if(target.equals(bbef)) {
                    boolean success = true;

                    final BitIterator bi = new BitIterator();
                    while(bi.hasNext()) {
                        final int inBef = bi.getNext(bbef);
                        final int inAft = bi.getNext(baft);

                        if(inBef != inAft) {

                        }
                    }

                    if(success) {
                        if(applyChanges) {

                        }
                        return true;
                    } else {
                        player.sendStatusMessage(new TranslationTextComponent("general."+ ChiselsAndBits2.MOD_ID+".undo.missing_bits"), true);
                        return false;
                    }
                }
            }
        } catch(Exception x) {
            x.printStackTrace();
            player.sendStatusMessage(new TranslationTextComponent("general."+ ChiselsAndBits2.MOD_ID+".undo.block_changed"), true);
        }
        return false;
    }

    private boolean inRange(final PlayerEntity player) {
        double reach = player.isCreative() ? 32 : 6;
        return player.getDistanceSq(new Vec3d(pos.getX(), pos.getY(), pos.getZ())) < reach * reach;
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
                pkt.handle(ctx.get().getSender(), true)
        );
        ctx.get().setPacketHandled(true);
    }
}
