package nl.dgoossens.chiselsandbits2.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.network.client.CChiselBlockPacket;
import nl.dgoossens.chiselsandbits2.common.network.client.CSetItemModePacket;
import nl.dgoossens.chiselsandbits2.common.network.client.CSetMenuActionModePacket;
import nl.dgoossens.chiselsandbits2.common.network.client.CUndoPacket;
import nl.dgoossens.chiselsandbits2.common.network.server.SAddUndoStep;
import nl.dgoossens.chiselsandbits2.common.network.server.SGroupMethod;
import nl.dgoossens.chiselsandbits2.common.network.server.SSynchronizeBitStoragePacket;

public class NetworkRouter {
    private final String PROTOCOL_VERSION = Integer.toString(1);
    private final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ChiselsAndBits2.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    /**
     * Registers all packets.
     */
    public void init() {
        int disc = 0;
        HANDLER.registerMessage(disc++, CChiselBlockPacket.class, CChiselBlockPacket::encode, CChiselBlockPacket::decode, CChiselBlockPacket::handle);
        HANDLER.registerMessage(disc++, CSetItemModePacket.class, CSetItemModePacket::encode, CSetItemModePacket::decode, CSetItemModePacket::handle);
        HANDLER.registerMessage(disc++, CSetMenuActionModePacket.class, CSetMenuActionModePacket::encode, CSetMenuActionModePacket::decode, CSetMenuActionModePacket::handle);
        HANDLER.registerMessage(disc++, CUndoPacket.class, CUndoPacket::encode, CUndoPacket::decode, CUndoPacket::handle);
        HANDLER.registerMessage(disc++, SSynchronizeBitStoragePacket.class, SSynchronizeBitStoragePacket::encode, SSynchronizeBitStoragePacket::decode, SSynchronizeBitStoragePacket::handle);
        HANDLER.registerMessage(disc++, SAddUndoStep.class, SAddUndoStep::encode, SAddUndoStep::decode, SAddUndoStep::handle);
        HANDLER.registerMessage(disc++, SGroupMethod.BeginGroup.class, SGroupMethod.BeginGroup::encode, SGroupMethod.BeginGroup::decode, SGroupMethod.BeginGroup::handle);
        HANDLER.registerMessage(disc++, SGroupMethod.EndGroup.class, SGroupMethod.EndGroup::encode, SGroupMethod.EndGroup::decode, SGroupMethod.EndGroup::handle);
    }

    /**
     * Sends the packet from the CLIENT to the SERVER.
     */
    public void sendToServer(final Object packet) {
        HANDLER.sendToServer(packet);
    }

    /**
     * Send the packet from SERVER to a given players CLIENT.
     */
    public void sendTo(final Object packet, final ServerPlayerEntity player) {
        HANDLER.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}