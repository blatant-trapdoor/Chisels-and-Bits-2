package nl.dgoossens.chiselsandbits2.common.impl;

import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.IItemModeType;

import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ItemModeType implements IItemModeType {
    //Type names must be identical to the startsWith() of the ItemMode!
    //Static Types
    CHISEL,
    PATTERN,
    TAPEMEASURE,
    WRENCH,
    BLUEPRINT,
    MALLET,
    CHISELED_BLOCK,

    //Dynamic Type
    SELECTED,
    ;

    ItemModeType() {
        ChiselsAndBits2.getInstance().getAPI().registerItemModeType(this);
    }

    private List<IItemMode> cache;

    @Override
    public List<IItemMode> getItemModes(final ItemStack item) {
        if (this == SELECTED)
            return item.getCapability(StorageCapabilityProvider.STORAGE).map(s -> s.listTypesAsItemModes(item.getItem())).orElse(new ArrayList<>());
        if (cache == null)
            cache = ChiselsAndBits2.getInstance().getAPI().getItemModes().parallelStream().filter(f -> f.getType() == this).collect(Collectors.toList());
        return cache;
    }

    @Override
    public boolean isDynamic() {
        return this == SELECTED;
    }
}
