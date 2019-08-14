package nl.dgoossens.chiselsandbits2.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ChiselEvent {
    /**
     * We track the last time the player clicked to chisel to determine when 300ms have passed before
     * we allow another click. This event is client-sided so we only need a single variable.
     */
    private static long lastClick = System.currentTimeMillis();

    /*@SubscribeEvent Waiting for Forge PR...
    public static void onClick(InputEvent.ClickInputEvent e) {
        if(e.isRightClick()) return;
        if(e.getRaytrace() == null || e.getRaytrace().getType() != RayTraceResult.Type.BLOCK) return;
        final PlayerEntity player = ChiselsAndBits2.getClient().getPlayer();
        if(!(player.getHeldItemMainhand().getItem() instanceof ChiselItem)) return;

        if(System.currentTimeMillis()-lastClick < 300) return;
        lastClick = System.currentTimeMillis();

        if(startChiselingBlock(e.getRaytrace(), ItemMode.getMode(ItemMode.Type.CHISEL, player.getHeldItemMainhand()), player))
            e.setCanceled(true);
    }*/
}
