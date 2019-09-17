package nl.dgoossens.chiselsandbits2.api;

/**
 * Represents a source of voxels, for example
 * the VoxelBlob.
 */
public interface IVoxelSrc {
    /**
     * Get the state id of the bit at the location.
     * Safe method, impossible coordinates will be clamped to
     * be valid.
     */
    int getSafe(
            int x,
            int y,
            int i);
}
