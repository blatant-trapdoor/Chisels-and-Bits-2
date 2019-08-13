package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

public enum VoxelType {
    AIR, SOLID, FLUID;

    /**
     * Whether or not the other voxeltype can be seen through this
     * voxel type. (used for culling tests)
     */
    public boolean shouldShow(final VoxelType other) {
        return this != AIR && this != other;
    }
}
