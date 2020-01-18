package nl.dgoossens.chiselsandbits2.api.render;

import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;

/**
 * An object referencing the state of an tile with a VoxelBlob.
 */
public interface IStateRef {
    /**
     * The voxel blob of this reference.
     */
    VoxelBlob getVoxelBlob();
}
