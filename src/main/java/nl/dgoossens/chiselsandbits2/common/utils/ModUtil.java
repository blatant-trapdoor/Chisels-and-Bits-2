package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.IntegerBox;

import javax.annotation.Nonnull;
import java.awt.*;

public class ModUtil {
    @Nonnull
    public static final String NBT_BLOCKENTITYTAG = "BlockEntityTag";
    //The amount of memory allocated to Minecraft.
    private static long memory = -1;

    /**
     * Returns true if less than 1256 MB in memory is allocated
     * to Minecraft.
     */
    public static boolean isLowMemoryMode() {
        if (memory == -1)
            memory = Runtime.getRuntime().maxMemory() / (1024 * 1024); // mb
        return memory < 1256;
    }

    /**
     * Get the blockstate corresponding to an id.
     * If this id does not correspond to a fluid state a random
     * blockstate could be returned.
     * Do not use unless you are sure that this is a blockstate!
     * (use {@link VoxelType#getType(int)}
     */
    public static BlockState getBlockState(final int blockStateId) {
        return Block.getStateById(blockStateId << 2 >>> 2); //Shift and shift back 2 to lose the identifier.
    }

    /**
     * Gets the fluidstate corresponding to an id.
     * If this id is not a fluid state, errors may occur.
     */
    public static IFluidState getFluidState(final int fluidStateId) {
        IFluidState ret = Fluid.STATE_REGISTRY.getByValue(fluidStateId << 2 >>> 2); //Shift and shift back 2 to lose the identifier.
        return ret == null ? Fluids.EMPTY.getDefaultState() : ret;
    }

    /**
     * Get the colour of a state id.
     * If this is not a colour results are unknown.
     */
    public static Color getColourState(final int colourStateId) {
        //We shift the alpha part 2 to the left (and back) to drop the identifier.
        //Then we multiply the alpha part by 4 to make use of the full range of visibility.
        //Then we add the normal part of the id to it, which makes the int complete in the way that the colour object internally stores it.
        return new Color((colourStateId << 2 >>> 2) * 4 + colourStateId << 8 >>> 8, true);
    }

    /**
     * Get a blockstate's id.
     */
    public static int getStateId(final BlockState state) {
        //Writing the identifier as these long numbers seems useless but it's necessary to keep java from screwing with them.
        return 0b11000000000000000000000000000000 + Math.max(0, Block.BLOCK_STATE_IDS.get(state));
    }

    /**
     * Get a fluidstate's id.
     */
    public static int getFluidId(final IFluidState fluid) {
        return 0b01000000000000000000000000000000 + Math.max(0, Fluid.STATE_REGISTRY.get(fluid));
    }

    /**
     * Get a colour's id.
     * Only 1/4 of the alpha is stored, nobody will notice
     * the small alpha differences anyways.
     */
    public static int getColourId(final Color colour) {
        //We built our own int instead of getting the colour value because we do some trickery to the MSBs.
        return 0b10000000000000000000000000000000 + ((colour.getAlpha() / 4) << 24) + (colour.getRed() << 16) + (colour.getGreen() << 8) + colour.getBlue();
    }

    /**
     * Calculates the partial offset used by ghost rendering.
     */
    public static BlockPos getPartialOffset(final Direction side, final BlockPos partial, final IntegerBox modelBounds ) {
        int offset_x = modelBounds.minX;
        int offset_y = modelBounds.minY;
        int offset_z = modelBounds.minZ;

        final int partial_x = partial.getX();
        final int partial_y = partial.getY();
        final int partial_z = partial.getZ();

        int middle_x = (modelBounds.maxX - modelBounds.minX) / -2;
        int middle_y = (modelBounds.maxY - modelBounds.minY) / -2;
        int middle_z = (modelBounds.maxZ - modelBounds.minZ) / -2;

        switch (side) {
            case DOWN:
                offset_y = modelBounds.maxY;
                middle_y = 0;
                break;
            case EAST:
                offset_x = modelBounds.minX;
                middle_x = 0;
                break;
            case NORTH:
                offset_z = modelBounds.maxZ;
                middle_z = 0;
                break;
            case SOUTH:
                offset_z = modelBounds.minZ;
                middle_z = 0;
                break;
            case UP:
                offset_y = modelBounds.minY;
                middle_y = 0;
                break;
            case WEST:
                offset_x = modelBounds.maxX;
                middle_x = 0;
                break;
            default:
                throw new NullPointerException();
        }

        final int t_x = -offset_x + middle_x + partial_x;
        final int t_y = -offset_y + middle_y + partial_y;
        final int t_z = -offset_z + middle_z + partial_z;

        return new BlockPos(t_x, t_y, t_z);
    }
}
