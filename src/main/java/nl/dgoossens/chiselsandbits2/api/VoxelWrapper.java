package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;
import java.awt.*;

/**
 * Represents the object of a VoxelType, can be either a Block, Fluid or Color.
 * This class serves as a bridge between these three classes which have no common
 * interface.
 */
public class VoxelWrapper<T> {
    private int id;
    private VoxelType type;

    private VoxelWrapper(Block b) {
        type = VoxelType.BLOCKSTATE;
        id = ModUtil.getStateId(b.getDefaultState());
    }

    private VoxelWrapper(Fluid f) {
        type = VoxelType.FLUIDSTATE;
        id = ModUtil.getFluidId(f.getDefaultState());
    }

    private VoxelWrapper(Color c) {
        type = VoxelType.COLOURED;
        id = ModUtil.getColourId(c);
    }

    private VoxelWrapper(int bit) {
        type = VoxelType.getType(bit);
        id = bit;
    }

    /**
     * Returned the wrapped object, can be either Block, Fluid or Color.
     */
    @Nullable
    public T get() {
        switch(type) {
            case BLOCKSTATE: return (T) ModUtil.getBlockState(id).getBlock();
            case FLUIDSTATE: return (T) ModUtil.getFluidState(id).getFluid();
            case COLOURED: return (T) ModUtil.getColourState(id);
            default: return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof VoxelWrapper) {
            VoxelWrapper o = (VoxelWrapper) obj;
            if(type != o.type) return false;
            //Use equals so we don't have multiple types of the same block in a bag.
            return get().equals(o.get());
        }
        return super.equals(obj);
    }

    /**
     * Get the bit id for this voxel wrapper.
     */
    public int getId() {
        return id;
    }

    /**
     * Creates an voxel object around an unknown voxel type.
     */
    public static <T> VoxelWrapper<T> forAbstract(int bit) {
        return new VoxelWrapper<>(bit);
    }

    /**
     * Creates an voxel object around a given block.
     */
    public static VoxelWrapper<Block> forBlock(Block b) {
        return new VoxelWrapper<>(b);
    }

    /**
     * Creates an voxel object around a given fluid.
     */
    public static VoxelWrapper<Fluid> forFluid(Fluid f) {
        return new VoxelWrapper<>(f);
    }

    /**
     * Creates an voxel object around a given colour.
     */
    public static VoxelWrapper<Color> forColor(Color c) {
        return new VoxelWrapper<>(c);
    }
}
