package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

public abstract class TypedItem extends Item implements IItemScrollWheel, IItemMenu {
    public TypedItem(Item.Properties builder) {
        super(builder);
    }

    /**
     * Display the mode in the highlight tip. (and color for tape measure)
     */
    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        IItemMode im = ItemModeUtil.getItemMode(item);
        return displayName + " - " + (im.equals(SelectedItemMode.NONE) ? I18n.format("general." + ChiselsAndBits2.MOD_ID + ".none") : im.getLocalizedName()) + ((getAssociatedType() == ItemModeType.TAPEMEASURE || getAssociatedType() == ItemModeType.CHISEL) ? (" - " + ItemModeUtil.getMenuActionMode(item).getLocalizedName()) : "");
    }

    /**
     * Scrolling on the chisel scrolls through the possible modes, alternative to the menu.
     */
    @Override
    public boolean scroll(final PlayerEntity player, final ItemStack stack, final double dwheel) {
        final IItemMode mode = ItemModeUtil.getHeldItemMode(player);
        ItemModeUtil.scrollOption(player, mode, stack, dwheel);
        return true;
    }
}
