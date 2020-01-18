package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.stats.IStatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

public class ModStatistics {
    public final ResourceLocation BITS_CHISELED = registerCustom("bits_chiseled", IStatFormatter.DEFAULT);

    //Copied from Stats.java
    private ResourceLocation registerCustom(String key, IStatFormatter formatter) {
        ResourceLocation resourcelocation = new ResourceLocation(ChiselsAndBits2.MOD_ID, key);
        Registry.register(Registry.CUSTOM_STAT, key, resourcelocation);
        Stats.CUSTOM.get(resourcelocation, formatter);
        return resourcelocation;
    }
}
