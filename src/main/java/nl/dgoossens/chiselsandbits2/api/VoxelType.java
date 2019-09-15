package nl.dgoossens.chiselsandbits2.api;

public enum VoxelType {
    BLOCKSTATE,
    COLOURED,
    FLUIDSTATE,
    AIR,
    ;

    /**
     * Returns true if this voxel type can be collided with.
     */
    public boolean isSolid() {
        return this==BLOCKSTATE || this==COLOURED;
    }

    /**
     * Returns true if this voxel type is a fluid and can be swum in.
     */
    public boolean isFluid() {
        return this==FLUIDSTATE;
    }

    /**
     * Get the voxel type that corresponds to this bit.
     */
    public static VoxelType getType(int bit) {
        switch(bit >>> 30) {
            case 1: return FLUIDSTATE;
            case 2: return COLOURED;
            case 3: return BLOCKSTATE;
            default: return AIR;
        }
    }

    /**
     * Whether or not the other voxeltype can be seen through this
     * voxel type. (used for culling tests)
     */
    public boolean shouldShow(final VoxelType other) {
        return this != AIR && this != other;
    }
}
