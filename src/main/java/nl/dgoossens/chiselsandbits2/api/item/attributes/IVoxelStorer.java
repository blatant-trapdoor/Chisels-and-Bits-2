package nl.dgoossens.chiselsandbits2.api.item.attributes;

import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;

/**
 * A general interface for an item storing a voxel blob.
 */
public interface IVoxelStorer {
    /**
     * Get the voxel blob stored by this voxel storer.
     */
    VoxelBlob getVoxelBlob(ItemStack item);

    /**
     * Set the voxel blob of this voxel storer.
     */
    void setVoxelBlob(ItemStack item, VoxelBlob vb);
}
