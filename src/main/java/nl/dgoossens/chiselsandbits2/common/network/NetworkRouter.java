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
import nl.dgoossens.chiselsandbits2.common.network.server.SSynchronizeBitStoragePacket;

public class NetworkRouter {
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(ChiselsAndBits2.MOD_ID, "main_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public NetworkRouter() {
        int disc = 0;
        HANDLER.registerMessage(disc++, CChiselBlockPacket.class, CChiselBlockPacket::encode, CChiselBlockPacket::decode, CChiselBlockPacket::handle);
        HANDLER.registerMessage(disc++, CSetItemModePacket.class, CSetItemModePacket::encode, CSetItemModePacket::decode, CSetItemModePacket::handle);
        HANDLER.registerMessage(disc++, CSetMenuActionModePacket.class, CSetMenuActionModePacket::encode, CSetMenuActionModePacket::decode, CSetMenuActionModePacket::handle);
        HANDLER.registerMessage(disc++, SSynchronizeBitStoragePacket.class, SSynchronizeBitStoragePacket::encode, SSynchronizeBitStoragePacket::decode, SSynchronizeBitStoragePacket::handle);
    }

    /**
     * Sends the packet from the CLIENT to the SERVER.
     */
    public static void sendToServer(final Object packet) {
        HANDLER.sendToServer(packet);
    }

    /**
     * Send the packet from SERVER to a given players CLIENT.
     */
    public static void sendTo(final Object packet, final ServerPlayerEntity player) {
        HANDLER.sendTo(packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
    }
}