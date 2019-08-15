package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.IItemScrollWheel;
import nl.dgoossens.chiselsandbits2.api.modes.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;

public abstract class TypedItem extends Item implements IItemScrollWheel, IItemMenu {
    public TypedItem(Item.Properties builder) { super(builder); }

    /**
     * Display the mode in the highlight tip. (and color for tape measure)
     */
    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        return displayName + " - " + ItemMode.getMode(item).getLocalizedName() + (getAssociatedType() == ItemMode.Type.TAPEMEASURE ? " - " + ItemMode.getColour(item).getLocalizedName() : "");
    }

    /**
     * Scrolling on the chisel scrolls through the possible modes, alternative to the menu.
     */
    @Override
    public boolean scroll(final PlayerEntity player, final ItemStack stack, final double dwheel) {
        final ItemMode mode = ChiselModeManager.getMode(player);
        ChiselModeManager.scrollOption(getAssociatedType(), mode, dwheel);
        return true;
    }
}
