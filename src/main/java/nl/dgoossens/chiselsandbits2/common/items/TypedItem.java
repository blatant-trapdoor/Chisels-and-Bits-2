package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;

public abstract class TypedItem extends Item implements IItemScrollWheel, IItemMenu {
    public TypedItem(Item.Properties builder) { super(builder); }

    /**
     * Display the mode in the highlight tip. (and color for tape measure)
     */
    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        return displayName + " - " + ChiselModeManager.getMode(item).getLocalizedName() + ((getAssociatedType() == ItemModeType.TAPEMEASURE || getAssociatedType() == ItemModeType.CHISEL) ? " - " + ChiselModeManager.getMenuActionMode(item).getLocalizedName() : "");
    }

    /**
     * Scrolling on the chisel scrolls through the possible modes, alternative to the menu.
     */
    @Override
    public boolean scroll(final PlayerEntity player, final ItemStack stack, final double dwheel) {
        final IItemMode mode = ChiselModeManager.getMode(player);
        ChiselModeManager.scrollOption(mode, dwheel);
        return true;
    }
}
