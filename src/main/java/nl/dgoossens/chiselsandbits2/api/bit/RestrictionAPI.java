package nl.dgoossens.chiselsandbits2.api.bit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;

import javax.annotation.Nullable;

/**
 * Restriction API for allowing blocks to be chiseled even if they are not seen as chiselable by default.
 */
public interface RestrictionAPI {
    /**
     * Set a block to be (not) chiselable regardless of its properties.
     */
    void setChiselable(final Block block, final boolean value);

    /**
     * Restricts blocks from being chiseled if they have the given property with the given values.
     * E.g. if a block (grass) has the SNOWY property set to true make it false
     *
     * @param target The value to set the property to, set to null to make the state unchiselable.
     * @param values The valid values that the property can be equal to at the moment for it to be changed to the target.
     */
    <T extends Comparable<T>> void restrictBlockStateProperty(final IProperty<T> property, @Nullable final T target, final T... values);

    /**
     * Get the block state that should be used whenever a blockstate of type block wants to be used in a chiseled block.
     */
    <T extends Comparable<T>> BlockState getPlacementState(final BlockState block);

    /**
     * Test if a blockstate can be chiseled.
     */
    boolean canChiselBlock(final BlockState block);
}
