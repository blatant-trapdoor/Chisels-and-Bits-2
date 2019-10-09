package nl.dgoossens.chiselsandbits2.common.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;

import java.util.function.Supplier;

public class PacketSetItemMode implements NetworkRouter.ModPacket {
    private IItemMode newMode;
    private ItemModeType type;

    private PacketSetItemMode() {
    }

    public PacketSetItemMode(final IItemMode mode) {
        newMode = mode;
        type = mode.getType();
    }

    public static void encode(PacketSetItemMode msg, PacketBuffer buf) {
        buf.writeInt(msg.type.ordinal());
        buf.writeString(msg.newMode.getName());
    }

    public static PacketSetItemMode decode(PacketBuffer buffer) {
        PacketSetItemMode pc = new PacketSetItemMode();
        pc.type = ItemModeType.values()[buffer.readInt()];
        try {
            pc.newMode = ChiselModeManager.resolveMode(buffer.readString(),
                    pc.type == ItemModeType.SELECTED_BLOCK ? new ItemStack(ChiselsAndBits2.getInstance().getItems().BIT_BAG) :
                    pc.type == ItemModeType.SELECTED_FLUID ? new ItemStack(ChiselsAndBits2.getInstance().getItems().BIT_BEAKER) :
                    pc.type == ItemModeType.SELECTED_BOOKMARK ? new ItemStack(ChiselsAndBits2.getInstance().getItems().PALETTE) :
                    null);
        } catch (final Exception x) {
            x.printStackTrace();
        }
        return pc;
    }

    public static class Handler {
        public static void handle(final PacketSetItemMode pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                if (pkt.newMode == null) return;
                ServerPlayerEntity player = ctx.get().getSender();
                final ItemStack ei = player.getHeldItemMainhand();
                if (ei.getItem() instanceof IItemMenu && pkt.type == ChiselModeManager.getMode(ei).getType()) {
                    ChiselModeManager.setMode(ei, pkt.newMode);
                    Minecraft.getInstance().player.sendStatusMessage(new StringTextComponent(ei.getItem().getHighlightTip(ei, ei.getDisplayName().getFormattedText())), true);
                }
            });
        }
    }
}
