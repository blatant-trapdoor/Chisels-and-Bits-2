package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.SelectedItemMode;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

import java.util.function.Supplier;

/**
 * Executes the selection of an item mode.
 * Sent CLIENT -> SERVER.
 */
public class CSetItemModePacket {
    private IItemMode newMode;
    private IItemModeType type;

    private CSetItemModePacket() {}
    public CSetItemModePacket(final IItemMode mode) {
        newMode = mode;
        type = mode.getType();
    }

    public static void encode(CSetItemModePacket msg, PacketBuffer buf) {
        buf.writeString(msg.type.name());
        buf.writeBoolean(msg.type.isDynamic());
        buf.writeInt(!msg.type.isDynamic() ? 0 : msg.newMode.getDynamicId());
        buf.writeString(msg.newMode.getName());
    }

    public static CSetItemModePacket decode(PacketBuffer buffer) {
        CSetItemModePacket pc = new CSetItemModePacket();
        String type = buffer.readString(256);
        for(IItemModeType t : ChiselsAndBits2.getInstance().getAPI().getItemPropertyRegistry().getModeTypes()) {
            if(t.name().equals(type)) {
                pc.type = t;
                break;
            }
        }
        try {
            boolean dynamic = buffer.readBoolean();
            int dynamicId = buffer.readInt();
            pc.newMode = ItemModeUtil.resolveMode(null, buffer.readString(256), dynamic, dynamicId);
        } catch (final Exception x) {
            x.printStackTrace();
        }
        return pc;
    }

    public static void handle(final CSetItemModePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (pkt.newMode == null || pkt.type == null) return;
            ServerPlayerEntity player = ctx.get().getSender();
            if (pkt.isValid(player))
                ItemModeUtil.setMode(player, player.getHeldItemMainhand(), pkt.newMode, !SelectedItemMode.isNone(pkt.newMode)); //Don't update timestamp if this is empty.
        });
        ctx.get().setPacketHandled(true);
    }

    public boolean isValid(PlayerEntity player) {
        if(player == null) return false;
        final ItemStack ei = player.getHeldItemMainhand();
        return (ei.getItem() instanceof IItemMenu && type == ItemModeUtil.getItemMode(ei).getType());
    }
}
