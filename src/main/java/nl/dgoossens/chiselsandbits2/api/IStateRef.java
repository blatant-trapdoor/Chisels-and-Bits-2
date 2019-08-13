package nl.dgoossens.chiselsandbits2.api;

import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;

/**
 * An object referencing the state of an tile with a VoxelBlob.
 */
public interface IStateRef {
	VoxelBlob getVoxelBlob();
}
