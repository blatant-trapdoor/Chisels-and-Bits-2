package nl.dgoossens.chiselsandbits2.common.impl.item;

import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;

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
    ;

    private List<IItemMode> cache;

    @Override
    public ItemMode getDefault() {
        switch(this) {
            case CHISEL: return ItemMode.CHISEL_SINGLE;
            case PATTERN: return ItemMode.PATTERN_REPLACE;
            case TAPEMEASURE: return ItemMode.TAPEMEASURE_BIT;
            case WRENCH: return ItemMode.WRENCH_ROTATE;
            case BLUEPRINT: return ItemMode.BLUEPRINT_UNKNOWN;
            case MALLET: return ItemMode.MALLET_UNKNOWN;
            case CHISELED_BLOCK: return ItemMode.CHISELED_BLOCK_FIT;
        }
        throw new UnsupportedOperationException("No default given for item mode type "+this);
    }
}
