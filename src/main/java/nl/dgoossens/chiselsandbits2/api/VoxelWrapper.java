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
    private Block block;
    private Fluid fluid;
    private Color colour;
    private int id;
    private VoxelType type;

    private VoxelWrapper(Block b) {
        type = VoxelType.BLOCKSTATE;
        block = b;
    }

    private VoxelWrapper(Fluid f) {
        type = VoxelType.FLUIDSTATE;
        fluid = f;
    }

    private VoxelWrapper(Color c) {
        type = VoxelType.COLOURED;
        colour = c;
    }

    private VoxelWrapper(int bit) {
        type = VoxelType.getType(bit);
        switch(type) {
            case BLOCKSTATE: block = ModUtil.getBlockState(bit).getBlock();
            case FLUIDSTATE: fluid = ModUtil.getFluidState(bit).getFluid();
            case COLOURED: colour = ModUtil.getColourState(bit);
        }
    }

    /**
     * Returned the wrapped object, can be either Block, Fluid or Color.
     */
    @Nullable
    public T get() {
        switch(type) {
            case BLOCKSTATE: return (T) block;
            case FLUIDSTATE: return (T) fluid;
            case COLOURED: return (T) colour;
            default: return null;
        }
    }

    /**
     * Get the bit id for this voxel wrapper.
     */
    public int getId() {
        if(id != 0) return id;
        switch(type) {
            case BLOCKSTATE:
                id = ModUtil.getStateId(block.getDefaultState());
                break;
            case FLUIDSTATE:
                id = ModUtil.getFluidId(fluid.getDefaultState());
                break;
            case COLOURED:
                id = ModUtil.getColourId(colour);
                break;
            default: return VoxelBlob.AIR_BIT;
        }
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
