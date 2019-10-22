package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.MenuAction;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;

import java.util.function.Supplier;

/**
 * Executes the selection of a menu action.
 * Sent CLIENT -> SERVER.
 */
public class CSetMenuActionModePacket {
    private MenuAction newMode;

    private CSetMenuActionModePacket() {
    }

    public CSetMenuActionModePacket(final MenuAction mode) {
        newMode = mode;
    }

    public static void encode(CSetMenuActionModePacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.newMode.ordinal());
    }

    public static CSetMenuActionModePacket decode(PacketBuffer buffer) {
        CSetMenuActionModePacket pc = new CSetMenuActionModePacket();
        pc.newMode = MenuAction.values()[buffer.readVarInt()];
        return pc;
    }

    public static void handle(final CSetMenuActionModePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            //Only tape measures support colour at the moment.
            if (pkt.isValid(player)) {
                ChiselModeManager.setMenuActionMode(player.getHeldItemMainhand(), pkt.newMode);
                //Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(ei.getItem().getHighlightTip(ei, ei.getDisplayName().getFormattedText())), true);
            }
        });
    }

    public boolean isValid(PlayerEntity player) {
        if(player == null) return false;
        final ItemStack ei = player.getHeldItemMainhand();
        return (ei.getItem() instanceof TapeMeasureItem || ei.getItem() instanceof ChiselHandler.BitModifyItem) && ChiselModeManager.getMode(ei).getType() == newMode.getAssociatedType();
    }
}
