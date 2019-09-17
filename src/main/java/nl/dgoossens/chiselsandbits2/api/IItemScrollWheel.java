package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface IItemScrollWheel {
    /**
     * Called whenever the player scrolls with the item in their hand
     * whilst shifting.
     */
    boolean scroll(PlayerEntity player, ItemStack stack, double dwheel);
}
