package nl.dgoossens.chiselsandbits2.api.item;

/**
 * Generic interface for a type of item mode.
 */
public interface IItemModeType {
    /**
     * Class implementing IItemModeType should be an enum.
     */
    String name();

    /**
     * Get the default value for this type.
     */
    IItemMode getDefault();
}
