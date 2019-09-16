package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BagCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BeakerCapabilityProvider;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ItemModeType {
    //Type names must be identical to the startsWith() of the ItemMode!
    CHISEL,
    PATTERN,
    TAPEMEASURE,
    WRENCH,
    BLUEPRINT,
    MALLET,

    SELECTED_BLOCK,
    SELECTED_FLUID,
    ;

    private List<IItemMode> cache;

    /**
     * Get all item modes associated with this type.
     */
    public List<IItemMode> getItemModes(final ItemStack item) {
        if(this==SELECTED_BLOCK)
            return item.getCapability(BagCapabilityProvider.BAG_STORAGE).map(BagStorage::listTypesAsItemModes).orElse(new ArrayList<>());
        if(this==SELECTED_FLUID)
            return item.getCapability(BeakerCapabilityProvider.BEAKER_STORAGE).map(BeakerStorage::listTypesAsItemModes).orElse(new ArrayList<>());
        if(cache==null) cache = Stream.of(ItemMode.values()).filter(f -> f.name().startsWith(name())).collect(Collectors.toList());
        return cache;
    }
}
