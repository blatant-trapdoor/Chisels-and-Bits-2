package nl.dgoossens.chiselsandbits2.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.network.NetworkRouter;

import java.util.function.Supplier;

public class PacketSetChiselMode implements NetworkRouter.ModPacket {
    private ItemMode newMode;
    private PacketSetChiselMode() {}
    public PacketSetChiselMode(final ItemMode mode) { newMode=mode; }

    public static void encode(PacketSetChiselMode msg, PacketBuffer buf) {
        buf.writeVarInt( msg.newMode.ordinal() );
    }

    public static PacketSetChiselMode decode(PacketBuffer buffer)
    {
        PacketSetChiselMode pc = new PacketSetChiselMode();
        pc.newMode = ItemMode.values()[buffer.readVarInt()];
        return pc;
    }

    public static class Handler
    {
        public static void handle(final PacketSetChiselMode pkt, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                final ItemStack ei = player.getHeldItemMainhand();
                if ( ei != null && ei.getItem() instanceof ChiselItem)
                {
                    final ItemMode originalMode = ItemMode.getMode( ei );
                    pkt.newMode.setMode( ei );

                    if ( originalMode != pkt.newMode) {
                        Minecraft.getInstance().player.sendStatusMessage( new StringTextComponent(ei.getItem().getHighlightTip(ei, ei.getDisplayName().getFormattedText())), true );
                    }
                }
            });
        }
    }
}
