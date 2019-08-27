package nl.dgoossens.chiselsandbits2.api;

import java.util.Set;

public interface IItemMenu {
	/**
	 * Get the item modes that should be displayed
	 * in this item's item menu.
	 */
	default Set<IItemMode> getItemModes() {
		return getAssociatedType().getItemModes();
	}

	/**
	 * The type of this item, used to automatically determine
	 * the contents of the menu.
	 */
	ItemModeType getAssociatedType();
}
