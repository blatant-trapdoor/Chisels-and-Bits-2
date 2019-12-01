package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.block.BitOperation;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

import java.util.function.Supplier;

/**
 * Executes the chiseling action.
 * Sent CLIENT -> SERVER.
 */
public class CChiselBlockPacket {
    public BitLocation from;
    public BitLocation to;
    public BitOperation operation;
    public Direction side;
    public IItemMode mode;
    public int placedBit;

    private CChiselBlockPacket() {}

    public CChiselBlockPacket(final BitOperation operation, final BitLocation from, final BitLocation to, final Direction side, final IItemMode mode, final int placedBit) {
        this.operation = operation;
        this.from = BitLocation.min(from, to);
        this.to = BitLocation.max(from, to);
        this.side = side;
        this.mode = mode;
        this.placedBit = placedBit;
    }

    public CChiselBlockPacket(final BitOperation operation, final BitLocation location, final Direction side, final IItemMode mode, final int placedBit) {
        this.operation = operation;
        from = to = location;
        this.side = side;
        this.mode = mode;
        this.placedBit = placedBit;
    }

    private static BitLocation readBitLoc(final PacketBuffer buffer) {
        return new BitLocation(buffer.readBlockPos(), buffer.readByte(), buffer.readByte(), buffer.readByte());
    }

    private static void writeBitLoc(final BitLocation from2, final PacketBuffer buffer) {
        buffer.writeBlockPos(from2.blockPos);
        buffer.writeByte(from2.bitX);
        buffer.writeByte(from2.bitY);
        buffer.writeByte(from2.bitZ);
    }

    public static void encode(CChiselBlockPacket msg, PacketBuffer buf) {
        writeBitLoc(msg.from, buf);
        writeBitLoc(msg.to, buf);

        buf.writeVarInt(msg.placedBit);
        buf.writeEnumValue(msg.operation);
        buf.writeVarInt(msg.side.ordinal());
        buf.writeBoolean(msg.mode.getType().isDynamic());
        buf.writeVarInt(msg.mode.getDynamicId());
        buf.writeString(msg.mode.getName());
    }

    public static CChiselBlockPacket decode(PacketBuffer buffer) {
        CChiselBlockPacket pc = new CChiselBlockPacket();
        pc.from = readBitLoc(buffer);
        pc.to = readBitLoc(buffer);

        pc.placedBit = buffer.readVarInt();
        pc.operation = buffer.readEnumValue(BitOperation.class);
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

    public static void handle(final CChiselBlockPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChiselHandler.handle(pkt, ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }
}
