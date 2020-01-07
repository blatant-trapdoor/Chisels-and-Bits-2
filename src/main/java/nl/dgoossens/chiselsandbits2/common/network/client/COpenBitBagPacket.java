package nl.dgoossens.chiselsandbits2.common.network.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.common.impl.ItemMode;
import nl.dgoossens.chiselsandbits2.common.utils.ItemPropertyUtil;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Used to notify the server that a bit bag was opened.
 * Sent CLIENT -> SERVER.
 */
public class COpenBitBagPacket {
    private Hand hand;

    private COpenBitBagPacket() {}
    public COpenBitBagPacket(final Hand hand) {
        this.hand = hand;
    }

    public static void encode(COpenBitBagPacket msg, PacketBuffer buf) {
        buf.writeVarInt(msg.hand.ordinal());
    }

    public static COpenBitBagPacket decode(PacketBuffer buffer) {
        COpenBitBagPacket pc = new COpenBitBagPacket();
        pc.hand = Hand.values()[buffer.readVarInt()];
        return pc;
    }

    public static void handle(final COpenBitBagPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ctx.get().getSender().openContainer(new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return null;
                }

                @Nullable
                @Override
                public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
                    return null;
                }
            })
        });
        ctx.get().setPacketHandled(true);
    }
}
