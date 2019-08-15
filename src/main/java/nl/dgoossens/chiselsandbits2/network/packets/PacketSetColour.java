package nl.dgoossens.chiselsandbits2.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;
import nl.dgoossens.chiselsandbits2.api.modes.MenuAction;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;
import nl.dgoossens.chiselsandbits2.network.NetworkRouter;

import java.util.function.Supplier;

public class PacketSetColour implements NetworkRouter.ModPacket {
    private MenuAction newMode;
    private PacketSetColour() {}
    public PacketSetColour(final MenuAction mode) { newMode=mode; }

    public static void encode(PacketSetColour msg, PacketBuffer buf) {
        buf.writeVarInt( msg.newMode.ordinal() );
    }

    public static PacketSetColour decode(PacketBuffer buffer)
    {
        PacketSetColour pc = new PacketSetColour();
        pc.newMode = MenuAction.values()[buffer.readVarInt()];
        return pc;
    }

    public static class Handler
    {
        public static void handle(final PacketSetColour pkt, Supplier<NetworkEvent.Context> ctx)
        {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                final ItemStack ei = player.getHeldItemMainhand();
                //Only tape measures support colour at the moment.
                if(ei != null && ei.getItem() instanceof TapeMeasureItem && ItemMode.Type.TAPEMEASURE==ItemMode.getMode(ei).getType()) {
                    ItemMode.setColour(ei, pkt.newMode.getDyeColour());
                    Minecraft.getInstance().player.sendStatusMessage( new StringTextComponent(ei.getItem().getHighlightTip(ei, ei.getDisplayName().getFormattedText())), true );
                }
            });
        }
    }
}
