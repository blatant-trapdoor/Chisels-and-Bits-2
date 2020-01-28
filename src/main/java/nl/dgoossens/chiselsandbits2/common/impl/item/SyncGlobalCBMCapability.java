package nl.dgoossens.chiselsandbits2.common.impl.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.network.server.SGlobalCBMPacket;

@Mod.EventBusSubscriber
public class SyncGlobalCBMCapability {
    @SubscribeEvent
    public static void persistCapability(PlayerEvent.Clone e) {
        //Clone GLOBAL_CBM over from old entity to new one if possible
        e.getOriginal().getCapability(GlobalCBMCapabilityProvider.GLOBAL_CBM).ifPresent(cap -> {
            e.getEntity().getCapability(GlobalCBMCapabilityProvider.GLOBAL_CBM).ifPresent(c -> {
                c.insert(cap.get());
            });
        });
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if(e.getPlayer().getEntityWorld().isRemote) return;

        //Only send player the capability on a server.
        e.getPlayer().getCapability(GlobalCBMCapabilityProvider.GLOBAL_CBM).ifPresent(cap -> {
            ChiselsAndBits2.getInstance().getNetworkRouter().sendTo(new SGlobalCBMPacket(cap.get()), (ServerPlayerEntity) e.getPlayer());
        });
    }

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> e) {
        //Attach the capability where we store the global chiseled block mode
        if(e.getObject().getType() == EntityType.PLAYER)
            e.addCapability(new ResourceLocation(ChiselsAndBits2.MOD_ID, "global_cbm"), new GlobalCBMCapabilityProvider());
    }
}
