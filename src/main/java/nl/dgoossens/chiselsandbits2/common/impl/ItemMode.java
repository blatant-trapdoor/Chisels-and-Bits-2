package nl.dgoossens.chiselsandbits2.common.impl;

import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.item.ItemModeEnum;

/**
 * The current mode the item is using shared between all item mode types in base C&B2.
 */
public enum ItemMode implements ItemModeEnum {
    CHISEL_SINGLE,
    CHISEL_LINE,
    CHISEL_PLANE,
    CHISEL_CONNECTED_PLANE,
    CHISEL_CONNECTED_MATERIAL,
    CHISEL_DRAWN_REGION,
    CHISEL_SAME_MATERIAL,
    CHISEL_SNAP8,
    CHISEL_SNAP4,
    CHISEL_SNAP2,
    CHISEL_CUBE3,
    CHISEL_CUBE5,
    CHISEL_CUBE7,

    PATTERN_REPLACE, //I've actually rarely used patterns myself so I'll go experiment with them once I fromName around to them, then I'll probably add some more modes.
    PATTERN_ADDITIVE,
    PATTERN_PLACEMENT,
    PATTERN_IMPOSE,

    TAPEMEASURE_BIT,
    TAPEMEASURE_BLOCK,
    TAPEMEASURE_DISTANCE,

    WRENCH_ROTATE,
    WRENCH_ROTATECCW,
    WRENCH_MIRROR,

    BLUEPRINT_UNKNOWN,

    MALLET_UNKNOWN,

    CHISELED_BLOCK_GRID, //Can't place chiseled block in spaces that already contain chiseled blocks
    CHISELED_BLOCK_FIT, //Can only place chiseled blocks if there is no overlap with existing blocks
    CHISELED_BLOCK_OVERLAP, //Place chiseled blocks and replace existing bits
    CHISELED_BLOCK_MERGE, //Merge chiseled blocks and don't place bits in spots where bits already exist
    ;

    private IItemModeType type;
    private String typelessName;

    ItemMode() {
        //Hardcore the chiseled block as it starts with CHISEL which can mess up
        //We can get all types now because this enum doesn't contain those that don't use types already registered.
        type = calculateType();
        typelessName = name().substring(getType().name().length() + 1).toLowerCase();
    }

    //Cache typeless name for improved performance
    @Override
    public String getTypelessName() {
        return typelessName;
    }

    //We don't always calculate the type like this, we cache it.
    private IItemModeType calculateType() {
        switch(this) {
            case CHISEL_SINGLE:
            case CHISEL_LINE:
            case CHISEL_PLANE:
            case CHISEL_CONNECTED_PLANE:
            case CHISEL_CONNECTED_MATERIAL:
            case CHISEL_DRAWN_REGION:
            case CHISEL_SAME_MATERIAL:
            case CHISEL_SNAP8:
            case CHISEL_SNAP4:
            case CHISEL_SNAP2:
            case CHISEL_CUBE3:
            case CHISEL_CUBE5:
            case CHISEL_CUBE7:
                return ItemModeType.CHISEL;
            case PATTERN_REPLACE:
            case PATTERN_ADDITIVE:
            case PATTERN_PLACEMENT:
            case PATTERN_IMPOSE:
                return ItemModeType.PATTERN;
            case TAPEMEASURE_BIT:
            case TAPEMEASURE_BLOCK:
            case TAPEMEASURE_DISTANCE:
                return ItemModeType.TAPEMEASURE;
            case WRENCH_ROTATE:
            case WRENCH_ROTATECCW:
            case WRENCH_MIRROR:
                return ItemModeType.WRENCH;
            case BLUEPRINT_UNKNOWN:
                return ItemModeType.BLUEPRINT;
            case MALLET_UNKNOWN:
                return ItemModeType.MALLET;
            case CHISELED_BLOCK_GRID:
            case CHISELED_BLOCK_FIT:
            case CHISELED_BLOCK_OVERLAP:
            case CHISELED_BLOCK_MERGE:
                return ItemModeType.CHISELED_BLOCK;
        }
        throw new UnsupportedOperationException("No type set for item mode "+name());
    }

    //We also cache the type.
    @Override
    public IItemModeType getType() {
        return type;
    }

    @Override
    public boolean hasIcon() {
        return this != MALLET_UNKNOWN && this != BLUEPRINT_UNKNOWN;
    }

    @Override
    public boolean hasHotkey() {
        switch (this) {
            case TAPEMEASURE_BIT: //Nobody will ever use tape measure hotkeys, they just take up space in the controls menu.
            case TAPEMEASURE_BLOCK:
            case TAPEMEASURE_DISTANCE:
            case BLUEPRINT_UNKNOWN:
            case MALLET_UNKNOWN:
            case CHISELED_BLOCK_FIT: //Can't hotkey these as they are global so you won't be switching them often, they are more for preference
            case CHISELED_BLOCK_GRID:
            case CHISELED_BLOCK_MERGE:
            case CHISELED_BLOCK_OVERLAP:
                return false;
            default:
                return true;
        }
    }
}
