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
    PATTERN_REPLACEMENT,
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
    ;

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
        return name().substring(getType().name().length() + 1).toLowerCase();
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
        return Stream.of(ItemModeType.values()).filter(f -> name().startsWith(f.name())).findAny().orElse(ItemModeType.CHISEL);
    }

    /**
     * Returns whether or not this mode has an icon.
     */
    public boolean hasIcon() {
        return true;
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
                return false;
            default:
                return true;
        }
    }
}
