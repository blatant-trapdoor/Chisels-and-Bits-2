package nl.dgoossens.chiselsandbits2.api.item.property;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * A single item property that an item can have.
 */
public interface IItemProperty<T> {
    /**
     * Get the value stored in this property.
     */
    T get(final ItemStack stack);

    /**
     * Set the value to a given value. Can be called on both sides,
     * world is used to determine whether or not we need to send
     * a packet to server or if we can already set data.
     */
    void set(final World world, final ItemStack stack, final T value);
}
