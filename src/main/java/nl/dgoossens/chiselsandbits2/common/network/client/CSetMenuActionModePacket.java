package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.item.IMenuAction;
import nl.dgoossens.chiselsandbits2.common.impl.MenuAction;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;

import java.util.function.Supplier;

/**
 * Executes the selection of a menu action.
 * Sent CLIENT -> SERVER.
 */
public class CSetMenuActionModePacket {
    private IMenuAction newMode;

    private CSetMenuActionModePacket() {
    }

    public CSetMenuActionModePacket(final IMenuAction mode) {
        newMode = mode;
    }

    public static void encode(CSetMenuActionModePacket msg, PacketBuffer buf) {
        buf.writeString(msg.newMode.name());
    }

    public static CSetMenuActionModePacket decode(PacketBuffer buffer) {
        CSetMenuActionModePacket pc = new CSetMenuActionModePacket();
        String s = buffer.readString();
        for(IMenuAction ima : ChiselsAndBits2.getInstance().getAPI().getItemPropertyRegistry().getMenuActions()) {
            if(ima.name().equals(s)) {
                pc.newMode = ima;
                break;
            }
        }
        return pc;
    }

    public static void handle(final CSetMenuActionModePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (pkt.isValid(player))
                ItemModeUtil.setMenuActionMode(player.getHeldItemMainhand(), pkt.newMode);
        });
        ctx.get().setPacketHandled(true);
    }

    public boolean isValid(PlayerEntity player) {
        if(player == null) return false;
        final ItemStack ei = player.getHeldItemMainhand();
        if(!(ei.getItem() instanceof IItemMenu)) return false;
        if(((IItemMenu) ei.getItem()).getMenuButtons(ei) == null) return false;
        IMenuAction f = ItemModeUtil.getMenuActionMode(ei);
        return ((IItemMenu) ei.getItem()).getMenuButtons(ei).parallelStream()
                    .anyMatch(e -> e.getMenuAction().equals(f));
    }
}
