package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;
import nl.dgoossens.chiselsandbits2.api.bit.BitLocation;
import nl.dgoossens.chiselsandbits2.common.impl.item.ItemMode;

import java.util.function.Supplier;

/**
 * Places a chiseled block.
 * Sent CLIENT -> SERVER.
 */
public class CPlaceBlockPacket {
    public BitLocation location;
    public BlockPos pos;
    public Direction side;
    public ItemMode mode;
    public boolean offgrid;

    private CPlaceBlockPacket() {}

    public CPlaceBlockPacket(final BlockPos pos, final BitLocation location, final Direction side, final ItemMode mode, final boolean offgrid) {
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
        buf.writeVarInt(msg.mode.ordinal());
    }

    public static CPlaceBlockPacket decode(PacketBuffer buffer) {
        CPlaceBlockPacket pc = new CPlaceBlockPacket();
        pc.pos = buffer.readBlockPos();
        pc.offgrid = buffer.readBoolean();
        pc.location = CChiselBlockPacket.readBitLoc(buffer);
        pc.side = Direction.values()[buffer.readVarInt()];
        pc.mode = ItemMode.values()[buffer.readVarInt()];
        return pc;
    }

    public static void handle(final CPlaceBlockPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChiselHandler.handle(pkt, ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }
}
