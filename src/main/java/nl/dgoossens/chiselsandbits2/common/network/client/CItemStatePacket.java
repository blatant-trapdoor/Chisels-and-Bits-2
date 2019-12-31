package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.common.items.ChiselMimicItem;
import nl.dgoossens.chiselsandbits2.common.items.MorphingBitItem;
import nl.dgoossens.chiselsandbits2.common.utils.ClientItemPropertyUtil;

import java.util.function.Supplier;

/**
 * Send to set the item state.
 * Sent CLIENT -> SERVER.
 */
public class CItemStatePacket {
    private boolean state;

    private CItemStatePacket() {}
    public CItemStatePacket(final boolean state) {
        this.state = state;
    }

    public static void encode(CItemStatePacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.state);
    }

    public static CItemStatePacket decode(PacketBuffer buffer) {
        CItemStatePacket pc = new CItemStatePacket();
        pc.state = buffer.readBoolean();
        return pc;
    }

    public static void handle(final CItemStatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            ItemStack stack = player.getHeldItemMainhand();
            if(stack.getItem() instanceof MorphingBitItem)
                ((MorphingBitItem)stack.getItem()).setLocked(stack, pkt.state);
            else if(stack.getItem() instanceof ChiselMimicItem)
                ((ChiselMimicItem)stack.getItem()).setSwap(stack, pkt.state);
        });
        ctx.get().setPacketHandled(true);
    }
}
