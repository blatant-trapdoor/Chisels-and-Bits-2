package nl.dgoossens.chiselsandbits2.client.render.chiseledblock;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.cache.CacheType;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RenderingEvents {
    private static boolean lastFancy = false;

    @SubscribeEvent
    public static void nextFrame(final RenderWorldLastEvent e) {
        RenderingManager m = ChiselsAndBits2.getInstance().getClient().getRenderingManager();
        m.runJobs(m.getTracker().getNextFrameTasks());
        m.uploadVBOs();

        if (Minecraft.getInstance().gameSettings.fancyGraphics != lastFancy) {
            lastFancy = Minecraft.getInstance().gameSettings.fancyGraphics;
            CacheType.DEFAULT.call();
        }
    }
}
