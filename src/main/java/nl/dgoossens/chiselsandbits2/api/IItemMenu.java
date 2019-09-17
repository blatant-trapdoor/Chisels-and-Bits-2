package nl.dgoossens.chiselsandbits2.api;

public interface IItemMenu {
    /**
     * The type of this item, used to automatically determine
     * the contents of the menu.
     */
    ItemModeType getAssociatedType();
}
