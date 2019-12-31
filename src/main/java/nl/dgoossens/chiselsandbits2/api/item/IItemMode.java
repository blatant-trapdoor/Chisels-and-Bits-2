package nl.dgoossens.chiselsandbits2.api.item;

/**
 * A generic item mode which can be
 */
public interface IItemMode {
    /**
     * Get the name of this item mode as it can be stored in NBT.
     */
    String getName();

    /**
     * Get the localized key from this Item Mode.
     */
    String getLocalizedName();

    /**
     * Return getName() but without the type in front.
     */
    String getTypelessName();

    /**
     * Get the type of this item mode.
     */
    IItemModeType getType();
}
