package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.client.resources.I18n;

import java.util.stream.Stream;

/**
 * The current mode the item is using, shared between patterns and chisels.
 */
public enum ItemMode implements IItemMode {
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
    WRENCH_NUDGE_BIT,
    WRENCH_NUDGE_BLOCK,

    BLUEPRINT_UNKNOWN,

    MALLET_UNKNOWN,

    CHISELED_BLOCK_GRID, //Can't place chiseled block in spaces that already contain chiseled blocks
    CHISELED_BLOCK_FIT, //Can only place chiseled blocks if there is no overlap with existing blocks
    CHISELED_BLOCK_OVERLAP, //Place chiseled blocks and replace existing bits
    CHISELED_BLOCK_MERGE, //Merge chiseled blocks and don't place bits in spots where bits already exist
    ;

    private ItemModeType type;
    private String typelessName;
    ItemMode() {
        //Hardcore the chiseled block as it starts with CHISEL which can mess up
        type = name().startsWith("CHISELED_BLOCK") ? ItemModeType.CHISELED_BLOCK : Stream.of(ItemModeType.values()).filter(f -> name().startsWith(f.name())).findAny().orElse(ItemModeType.CHISEL);
        typelessName = name().substring(getType().name().length() + 1).toLowerCase();
    }

    /**
     * Get the localized key from this Item Mode.
     */
    public String getLocalizedName() {
        return I18n.format("general.chiselsandbits2.itemmode." + getTypelessName());
    }

    /**
     * Return this enum's name() but without the type in front.
     */
    public String getTypelessName() {
        return typelessName;
    }

    /**
     * Get the name of this item mode as it can be stored in NBT.
     */
    public String getName() {
        return name();
    }

    /**
     * Get this item mode's type. (associated with name())
     */
    public ItemModeType getType() {
        return type;
    }

    /**
     * Returns whether or not this mode has an icon.
     */
    public boolean hasIcon() {
        return this != MALLET_UNKNOWN && this != BLUEPRINT_UNKNOWN;
    }

    /**
     * Returns false when a item mode should not have a hotkey.
     */
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
