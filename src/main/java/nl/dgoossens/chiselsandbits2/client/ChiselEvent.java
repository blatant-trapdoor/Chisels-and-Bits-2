package nl.dgoossens.chiselsandbits2.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

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
        //TODO cancel middle click when holding chisel for pick block functionality (check if the keybind wasn't changed)
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
