package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * An item with support for having custom functionality when the player scrolls the mouse
 * wheel whilst sneaking whilst holding the item.
 */
public interface IItemScrollWheel {
    /**
     * Called whenever the player scrolls with the item in their hand
     * whilst shifting.
     */
    boolean scroll(PlayerEntity player, ItemStack stack, double dwheel);
}
