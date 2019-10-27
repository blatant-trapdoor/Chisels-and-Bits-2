package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.item.Item;

import java.util.List;

/**
 * Represents an interface capable of storing bits of any type.
 */
public interface BitStorage {
    /**
     * Lists the bits stored in this storage.
     */
    List<VoxelWrapper> list();

    /**
     * Returns a set of all types of blocks in this storage.
     * Lists them as SelectedBlockItemMode instances.
     */
    List<IItemMode> listTypesAsItemModes(Item item);

    /**
     * Adds a wrapper to the first empty slot.
     * @return How much couldn't be put into the bag.
     */
    long add(final VoxelWrapper w, final long amount);

    /**
     * Sets a given voxel type to a given amount.
     */
    void set(final VoxelWrapper w, final long amount);

    /**
     * Returns whether this storage stores a given voxel type.
     */
    boolean has(VoxelWrapper b);

    /**
     * Gets the current amount of the voxel type.
     */
    long get(final VoxelWrapper w);

    /**
     * Return the amount of bits of a given voxel type that can be stored.
     */
    long queryRoom(final VoxelWrapper w);

    /**
     * Get the slot a given voxel wrapper is located in. If it has no slot this method
     * will return -1.
     */
    int getSlot(final VoxelWrapper w);

    /**
     * Sets a given slot to a given amount.
     */
    void setSlot(final int index, final VoxelWrapper w, final long amount);

    /**
     * Removes the given slot.
     */
    void clearSlot(final int index);

    /**
     * Gets the voxel typ this storage is limited to.
     */
    VoxelType getType();

    /**
     * Set the voxel type this storage is limited to.
     */
    void setType(VoxelType voxelType);
}
