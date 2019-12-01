package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.client.resources.I18n;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

/**
 * A general interface that can be extended by any enum that wants to register new item modes.
 * Register this using {@link ChiselsAndBitsAPI#registerItemMode(ItemModeEnum)}
 */
public interface ItemModeEnum extends IItemMode {
    /**
     * Default method for enums.
     */
    String name();

    /**
     * Get the localized key from this Item Mode.
     */
    public default String getLocalizedName() {
        return I18n.format("general.chiselsandbits2.itemmode." + getTypelessName());
    }

    /**
     * Get the name of this item mode as it can be stored in NBT.
     */
    public default String getName() {
        return name();
    }

    /**
     * Return this enum's name() but without the type in front.
     */
    public default String getTypelessName() {
        return name().substring(getType().name().length() + 1).toLowerCase();
    }

    /**
     * Get this item mode's type.
     */
    public default IItemModeType getType() {
        return ChiselsAndBits2.getInstance().getAPI().getItemModeTypes().parallelStream()
                .filter(f -> name().startsWith(f.name())).findAny().orElse(getDefaultType());
    }

    /**
     * Get the default type for this enum.
     */
    IItemModeType getDefaultType();

    /**
     * Returns whether or not this mode has an icon.
     */
    boolean hasIcon();

    /**
     * Returns false when a item mode should not have a hotkey.
     */
    boolean hasHotkey();
}
