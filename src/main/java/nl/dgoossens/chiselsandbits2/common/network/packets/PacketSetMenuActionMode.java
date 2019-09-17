package nl.dgoossens.chiselsandbits2.common.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.MenuAction;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;

import java.util.function.Supplier;

public class PacketSetMenuActionMode implements NetworkRouter.ModPacket {
    private MenuAction newMode;

    private PacketSetMenuActionMode() {
    }

    public PacketSetMenuActionMode(final MenuAction mode) {
        newMode = mode;
    }

    public static void encode(PacketSetMenuActionMode msg, PacketBuffer buf) {
        buf.writeVarInt(msg.newMode.ordinal());
    }

    public static PacketSetMenuActionMode decode(PacketBuffer buffer) {
        PacketSetMenuActionMode pc = new PacketSetMenuActionMode();
        pc.newMode = MenuAction.values()[buffer.readVarInt()];
        return pc;
    }

    public static class Handler {
        public static void handle(final PacketSetMenuActionMode pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayerEntity player = ctx.get().getSender();
                final ItemStack ei = player.getHeldItemMainhand();
                //Only tape measures support colour at the moment.
                if ((ei.getItem() instanceof TapeMeasureItem || ei.getItem() instanceof ChiselItem) && ChiselModeManager.getMode(ei).getType() == pkt.newMode.getAssociatedType()) {
                    ChiselModeManager.setMenuActionMode(ei, pkt.newMode);
                    Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(ei.getItem().getHighlightTip(ei, ei.getDisplayName().getFormattedText())), true);
                }
            });
        }
    }
}
