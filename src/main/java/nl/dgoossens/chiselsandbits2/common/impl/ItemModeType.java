package nl.dgoossens.chiselsandbits2.api;

import java.util.List;

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

    private List<IItemMode> cache;

    /**
     * Get all item modes associated with this type.
     *
    public List<IItemMode> getItemModes(final ItemStack item) {
        if (this == SELECTED)
            return item.getCapability(StorageCapabilityProvider.STORAGE).map(s -> s.listTypesAsItemModes(item.getItem())).orElse(new ArrayList<>());
        if (cache == null)
            cache = ChiselsAndBits2.getInstance().getAPI().getAllItemModes().parallelStream().filter(f -> f.getType() == this).collect(Collectors.toList());
        return cache;
    }*/

    @Override
    public boolean isDynamic() {
        return this == SELECTED;
    }
}
