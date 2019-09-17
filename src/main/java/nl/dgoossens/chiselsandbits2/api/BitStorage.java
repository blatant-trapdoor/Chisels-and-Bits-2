package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;

import java.awt.*;
import java.util.List;
import java.util.Set;

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
    List<IItemMode> listTypesAsItemModes();

    /**
     * Get the amount of block bits stored for a given block type.
     */
    long getAmount(final Block type);

    /**
     * Set the amount of block bits stored for a given block type.
     */
    void setAmount(final Block type, final long amount);

    /**
     * Get the amount of fluid bits stored for a given block type.
     */
    long getAmount(final Fluid type);

    /**
     * Set the amount of fluid bits stored for a given block type.
     */
    void setAmount(final Fluid type, final long amount);

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
}
