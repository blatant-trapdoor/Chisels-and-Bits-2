package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.common.items.ChiselMimicItem;
import nl.dgoossens.chiselsandbits2.common.items.MorphingBitItem;

import java.util.function.Supplier;

/**
 * Send to set the item state.
 * Sent CLIENT -> SERVER.
 */
public class CItemStatePacket {
    private boolean state;
    private boolean lock;

    private CItemStatePacket() {}
    public CItemStatePacket(final boolean state, final boolean lock) {
        this.state = state;
        this.lock = lock;
    }

    public static void encode(CItemStatePacket msg, PacketBuffer buf) {
        buf.writeBoolean(msg.state);
        buf.writeBoolean(msg.lock);
    }

    public static CItemStatePacket decode(PacketBuffer buffer) {
        CItemStatePacket pc = new CItemStatePacket();
        pc.state = buffer.readBoolean();
        pc.lock = buffer.readBoolean();
        return pc;
    }

    public static void handle(final CItemStatePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PlayerEntity player = ctx.get().getSender();
            ItemStack stack = player.getHeldItemMainhand();
            if(pkt.lock && stack.getItem() instanceof MorphingBitItem)
                ((MorphingBitItem)stack.getItem()).setLocked(player, stack, pkt.state);
            if(!pkt.lock && stack.getItem() instanceof ChiselMimicItem)
                ((ChiselMimicItem)stack.getItem()).setSwap(player, stack, pkt.state);
        });
        ctx.get().setPacketHandled(true);
    }
}
