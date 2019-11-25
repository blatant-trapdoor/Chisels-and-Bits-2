package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.ItemModeType;
import nl.dgoossens.chiselsandbits2.api.SelectedItemMode;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

import java.util.function.Supplier;

/**
 * Executes the selection of an item mode.
 * Sent CLIENT -> SERVER.
 */
public class CSetItemModePacket {
    private IItemMode newMode;
    private ItemModeType type;

    private CSetItemModePacket() {}
    public CSetItemModePacket(final IItemMode mode) {
        newMode = mode;
        type = mode.getType();
    }

    public static void encode(CSetItemModePacket msg, PacketBuffer buf) {
        buf.writeInt(msg.type.ordinal());
        buf.writeBoolean(msg.type.isDynamic());
        buf.writeInt(!msg.type.isDynamic() ? 0 : msg.newMode.getDynamicId());
        buf.writeString(msg.newMode.getName());
    }

    public static CSetItemModePacket decode(PacketBuffer buffer) {
        CSetItemModePacket pc = new CSetItemModePacket();
        pc.type = ItemModeType.values()[buffer.readInt()];
        try {
            boolean dynamic = buffer.readBoolean();
            int dynamicId = buffer.readInt();
            pc.newMode = ItemModeUtil.resolveMode(buffer.readString(), dynamic, dynamicId);
        } catch (final Exception x) {
            x.printStackTrace();
        }
        return pc;
    }

    public static void handle(final CSetItemModePacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (pkt.newMode == null) return;
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
