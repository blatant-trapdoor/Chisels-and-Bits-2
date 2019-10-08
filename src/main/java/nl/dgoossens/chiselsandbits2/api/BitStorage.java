package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.List;

public interface BitStorage {
    /**
     * Returns a list of all types of blocks in this storage.
     */
    List<Block> listBlocks();

    /**
     * Returns a list of all types of blocks in this storage.
     */
    List<Fluid> listFluids();

    /**
     * Returns a list of all bookmarked colours in this storage.
     */
    List<Color> listColours();

    /**
     * Returns a set of all types of blocks in this storage.
     * Lists them as SelectedBlockItemMode instances.
     */
    List<IItemMode> listTypesAsItemModes(Item item);

    /**
     * Get the amount of block bits stored for a given type.
     */
    long getAmount(final Block type);

    /**
     * Set the amount of block bits stored for a given type.
     */
    void setAmount(final Block type, final long amount);

    /**
     * Adds to the amount of block bits stored for a given type.
     * If this amount if negative and it would result in a negative amount of bits to remain we return the amount of bits we're short.
     */
    long addAmount(final Block type, final long amount);

    /**
     * Get the amount of fluid bits stored for a given type.
     */
    long getAmount(final Fluid type);

    /**
     * Set the amount of fluid bits stored for a given type.
     */
    void setAmount(final Fluid type, final long amount);

    /**
     * Adds to the amount of fluid bits stored for a given type.
     * If this amount if negative and it would result in a negative amount of bits to remain we return the amount of bits we're short.
     */
    long addAmount(final Fluid type, final long amount);

    /**
     * Adds a bookmark to a given slot.
     */
    void addBookmark(final Color color);

    /**
     * Sets a bookmark to a given slot.
     */
    void setBookmark(final int index, final Color color);

    /**
     * Removes the bookmark at a given slot.
     */
    void clearBookmark(final int index);

    /**
     * Returns whether this storage stores a given block.
     */
    boolean hasBlock(Block b);

    /**
     * Returns whether this storage stores a given fluid.
     */
    boolean hasFluid(Fluid f);
}
