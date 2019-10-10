package nl.dgoossens.chiselsandbits2.common.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.api.BitStorage;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BitStorageImpl;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Sent to the other dist to communicate changes to the bitstorage of an item.
 */
public class PacketSynchronizeBitStorage implements NetworkRouter.ModPacket {
    private INBT nbt;
    private int inventorySlot;

    private PacketSynchronizeBitStorage() {
    }

    public PacketSynchronizeBitStorage(final BitStorage bitStorage, final int inventorySlot) {
        nbt = StorageCapabilityProvider.STORAGE.writeNBT(bitStorage, null);
        this.inventorySlot = inventorySlot;
    }

    public static void encode(PacketSynchronizeBitStorage msg, PacketBuffer buf) {
        CompoundNBT compound = new CompoundNBT();
        compound.put("data", msg.nbt);
        buf.writeCompoundTag(compound);
        buf.writeInt(msg.inventorySlot);
    }

    public static PacketSynchronizeBitStorage decode(PacketBuffer buffer) {
        PacketSynchronizeBitStorage pc = new PacketSynchronizeBitStorage();
        CompoundNBT nbt = buffer.readCompoundTag();
        pc.nbt = nbt.get("data");
        pc.inventorySlot = buffer.readInt();
        return pc;
    }

    public static class Handler {
        public static void handle(final PacketSynchronizeBitStorage pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                PlayerEntity target;
                if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                    target = Minecraft.getInstance().player;
                } else {
                    target = ctx.get().getSender();
                }

                target.inventory.getStackInSlot(pkt.inventorySlot).getCapability(StorageCapabilityProvider.STORAGE).ifPresent(storage -> {
                    StorageCapabilityProvider.STORAGE.readNBT(storage, null, pkt.nbt);
                });
            });
        }
    }
}
