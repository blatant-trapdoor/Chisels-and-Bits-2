package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.Block;

import java.util.List;
import java.util.Set;

public interface BagStorage {
    /**
     * Returns a set of all types of blocks in this storage.
     */
    Set<Block> listTypes();
    /**
     * Returns a set of all types of blocks in this storage.
     * Lists them as SelectedBlockItemMode instances.
     */
    List<IItemMode> listTypesAsItemModes();
    /**
     * Get the amount of bits stored for a given block type.
     */
    long getAmount(final Block type);
    /**
     * Set the amount of bits stored for a given bit type.
     */
    void setAmount(final Block type, final long amount);
}
