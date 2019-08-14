package nl.dgoossens.chiselsandbits2.api;

import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;

import java.util.Set;

public interface IItemMenu {
	/**
	 * Get the item modes that should be displayed
	 * in this item's item menu.
	 */
	public default Set<ItemMode> getItemModes() {
		return getAssociatedType().getItemModes();
	}

	/**
	 * The type of this item, used to automatically determine
	 * the contents of the menu.
	 */
	ItemMode.Type getAssociatedType();
}
