package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

import java.util.function.Supplier;

/**
 * Executes a wrench action.
 * Sent CLIENT -> SERVER.
 */
public class CWrenchBlockPacket {
    public BlockPos pos;
    public Direction side;
    public IItemMode mode;

    private CWrenchBlockPacket() {}

    public CWrenchBlockPacket(final BlockPos pos, final Direction side, final IItemMode mode) {
        this.pos = pos;
        this.side = side;
        this.mode = mode;
    }

    public static void encode(CWrenchBlockPacket msg, PacketBuffer buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.side.ordinal());
        buf.writeBoolean(msg.mode.getType().isDynamic());
        buf.writeVarInt(msg.mode.getDynamicId());
        buf.writeString(msg.mode.getName());
    }

    public static CWrenchBlockPacket decode(PacketBuffer buffer) {
        CWrenchBlockPacket pc = new CWrenchBlockPacket();
        pc.pos = buffer.readBlockPos();
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

    public static void handle(final CWrenchBlockPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChiselHandler.handle(pkt, ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }
}
