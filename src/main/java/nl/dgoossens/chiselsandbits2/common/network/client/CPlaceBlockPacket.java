package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

import java.util.function.Supplier;

/**
 * Places a chiseled block.
 * Sent CLIENT -> SERVER.
 */
public class CPlaceBlockPacket {
    public BitLocation location;
    public BlockPos pos;
    public Direction side;
    public IItemMode mode;
    public boolean offgrid;

    private CPlaceBlockPacket() {}

    public CPlaceBlockPacket(final BlockPos pos, final BitLocation location, final Direction side, final IItemMode mode, final boolean offgrid) {
        this.pos = pos;
        this.location = location;
        this.side = side;
        this.mode = mode;
        this.offgrid = offgrid;
    }

    public static void encode(CPlaceBlockPacket msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.offgrid);
        CChiselBlockPacket.writeBitLoc(msg.location, buf);
        buf.writeVarInt(msg.side.ordinal());
        buf.writeBoolean(msg.mode.getType().isDynamic());
        buf.writeVarInt(msg.mode.getDynamicId());
        buf.writeString(msg.mode.getName());
    }

    public static CPlaceBlockPacket decode(PacketBuffer buffer) {
        CPlaceBlockPacket pc = new CPlaceBlockPacket();
        pc.pos = buffer.readBlockPos();
        pc.offgrid = buffer.readBoolean();
        pc.location = CChiselBlockPacket.readBitLoc(buffer);
        pc.side = Direction.values()[buffer.readVarInt()];
        try {
            boolean isDynamic = buffer.readBoolean();
            int dynamicId = buffer.readVarInt();
            pc.mode = ItemModeUtil.resolveMode(buffer.readString(256), isDynamic, dynamicId);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return pc;
    }

    public static void handle(final CPlaceBlockPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChiselHandler.handle(pkt, ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }
}
