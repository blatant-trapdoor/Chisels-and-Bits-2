package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;

import java.util.List;
import java.util.Set;

public interface BeakerStorage {
    /**
     * Returns a set of all types of fluids in this storage.
     */
    Set<Fluid> listTypes();
    /**
     * Returns a set of all types of blocks in this storage.
     * Lists them as SelectedBlockItemMode instances.
     */
    List<IItemMode> listTypesAsItemModes();
    /**
     * Get the amount of bits stored for a given fluid type.
     */
    long getAmount(final Fluid type);
    /**
     * Set the amount of bits stored for a given fluid type.
     */
    void setAmount(final Fluid type, final long amount);
}
