package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.item.DyedItemColour;
import nl.dgoossens.chiselsandbits2.common.items.ChiselMimicItem;
import nl.dgoossens.chiselsandbits2.common.items.MorphingBitItem;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;

import java.util.function.Supplier;

/**
 * Send to set the tape measure colour
 * Sent CLIENT -> SERVER.
 */
public class CTapeMeasureColour {
    private DyedItemColour state;

    private CTapeMeasureColour() {}
    public CTapeMeasureColour(final DyedItemColour state) {
        this.state = state;
    }

    public static void encode(CTapeMeasureColour msg, PacketBuffer buf) {
        buf.writeVarInt(msg.state.ordinal());
    }

    public static CTapeMeasureColour decode(PacketBuffer buffer) {
        CTapeMeasureColour pc = new CTapeMeasureColour();
        pc.state = DyedItemColour.values()[buffer.readVarInt()];
        return pc;
    }

    public static void handle(final CTapeMeasureColour pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            ItemStack stack = player.getHeldItemMainhand();
            if(stack.getItem() instanceof TapeMeasureItem)
                ((TapeMeasureItem) stack.getItem()).setColour(stack, pkt.state);
        });
        ctx.get().setPacketHandled(true);
    }
}
