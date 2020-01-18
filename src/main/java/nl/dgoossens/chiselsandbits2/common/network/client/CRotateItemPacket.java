package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;

import java.util.function.Supplier;

/**
 * Send by ROLL_X or ROLL_Z, causes
 * Sent CLIENT -> SERVER.
 */
public class CRotateItemPacket {
    private Direction.Axis axis;

    private CRotateItemPacket() {}
    public CRotateItemPacket(final Direction.Axis axis) {
        this.axis = axis;
    }

    public static void encode(CRotateItemPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.axis.ordinal());
    }

    public static CRotateItemPacket decode(PacketBuffer buffer) {
        CRotateItemPacket pc = new CRotateItemPacket();
        pc.axis = Direction.Axis.values()[buffer.readVarInt()];
        return pc;
    }

    public static void handle(final CRotateItemPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ChiselHandler.handle(pkt, ctx.get().getSender()));
        ctx.get().setPacketHandled(true);
    }

    public Direction.Axis getAxis() {
        return axis;
    }
}
