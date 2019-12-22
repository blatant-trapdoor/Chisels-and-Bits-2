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
    public Direction side;
    public IItemMode mode;
    public boolean offgrid;

    private CPlaceBlockPacket() {}

    public CPlaceBlockPacket(final BitLocation location, final Direction side, final IItemMode mode, final boolean offgrid) {
        this.location = location;
        this.side = side;
        this.mode = mode;
    }

    public static void encode(CPlaceBlockPacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.offgrid);
        CChiselBlockPacket.writeBitLoc(msg.location, buf);
        buf.writeVarInt(msg.side.ordinal());
        buf.writeBoolean(msg.mode.getType().isDynamic());
        buf.writeVarInt(msg.mode.getDynamicId());
        buf.writeString(msg.mode.getName());
    }

    public static CPlaceBlockPacket decode(PacketBuffer buffer) {
        CPlaceBlockPacket pc = new CPlaceBlockPacket();
        pc.offgrid = buffer.readBoolean();
        pc.location = CChiselBlockPacket.readBitLoc(buffer);
        pc.side = Direction.values()[buffer.readVarInt()];
        try {
            boolean isDynamic = buffer.readBoolean();
            int dynamicId = buffer.readVarInt();
            pc.mode = ItemModeUtil.resolveMode(buffer.readString(), isDynamic, dynamicId);
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
