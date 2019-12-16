package nl.dgoossens.chiselsandbits2.api.item;

import java.util.*;

/**
 * Registration is required as it is used to decodes packets on the server-side sent by the client
 * stating what was selected.
 */
public interface ItemPropertyAPI {
    /**
     * Registers a new value from an item mode enum. Can be used to add new modes to
     * item mode types.
     */
    void registerMode(final ItemModeEnum itemMode);

    /**
     * Register a new item mode type.
     */
    void registerModeType(final IItemModeType itemModeType);

    /**
     * Registers a new menu action.
     */
    void registerMenuAction(final IMenuAction menuAction);

    /**
     * Get a list of all existing item modes.
     */
    List<ItemModeEnum> getModes();

    /**
     * Get a list of all existing item mode types.
     */
    Set<IItemModeType> getModeTypes();

    /**
     * Get a set of all registered menu actions.
     */
    Set<IMenuAction> getMenuActions();
}
