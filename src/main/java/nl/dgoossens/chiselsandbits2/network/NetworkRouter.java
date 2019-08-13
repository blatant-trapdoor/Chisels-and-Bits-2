package nl.dgoossens.chiselsandbits2.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.network.packets.PacketChisel;
import nl.dgoossens.chiselsandbits2.network.packets.PacketSetChiselMode;

public class NetworkRouter
{
	private static final String PROTOCOL_VERSION = Integer.toString(1);
	private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
			.named(new ResourceLocation(ChiselsAndBits2.MOD_ID, "main_channel"))
			.clientAcceptedVersions(PROTOCOL_VERSION::equals)
			.serverAcceptedVersions(PROTOCOL_VERSION::equals)
			.networkProtocolVersion(() -> PROTOCOL_VERSION)
			.simpleChannel();


	public NetworkRouter() {
		int disc = 0;
		HANDLER.registerMessage(disc++, PacketChisel.class, PacketChisel::encode, PacketChisel::decode, PacketChisel.Handler::handle);
		HANDLER.registerMessage(disc++, PacketSetChiselMode.class, PacketSetChiselMode::encode, PacketSetChiselMode::decode, PacketSetChiselMode.Handler::handle);
	}

	/**
	 * from client to server
	 *
	 * @param packet
	 */
	public static void sendToServer(
			final ModPacket packet )
	{
		HANDLER.sendToServer( packet );
	}

	/**
	 * from server to clients...
	 *
	 * @param packet
	 */
	public static void sendToAll(
			final ModPacket packet )
	{
		for(ServerPlayerEntity spe : Minecraft.getInstance().getIntegratedServer().getPlayerList().getPlayers()) {
			sendTo(packet, spe);
		}
	}

	/**
	 * from server to specific client.
	 *
	 * @param packet
	 * @param player
	 */
	public static void sendTo(
			final ModPacket packet,
			final ServerPlayerEntity player )
	{
		HANDLER.sendTo( packet, player.connection.netManager, NetworkDirection.PLAY_TO_CLIENT );
	}

	public static interface ModPacket {}
}