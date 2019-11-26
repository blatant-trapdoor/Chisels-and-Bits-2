package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ItemModeType {
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

    private List<IItemMode> cache;

    /**
     * Get all item modes associated with this type.
     */
    public List<IItemMode> getItemModes(final ItemStack item) {
        if (this == SELECTED)
            return item.getCapability(StorageCapabilityProvider.STORAGE).map(s -> s.listTypesAsItemModes(item.getItem())).orElse(new ArrayList<>());
        if (cache == null)
            cache = Stream.of(ItemMode.values()).filter(f -> f.getType() == this).collect(Collectors.toList());
        return cache;
    }

    /**
     * Returns whether or not this type is "dynamic". Dynamic types are types where the item mode is an object and has more information
     * attached, static types are defined by the ItemMode enum.
     */
    public boolean isDynamic() {
        return this == SELECTED;
    }
}
