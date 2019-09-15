package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import nl.dgoossens.chiselsandbits2.api.VoxelType;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Random;

public class ModUtil {
    //The amount of memory allocated to Minecraft.
    private static long memory = -1;

    @Nonnull
    public static final String NBT_BLOCKENTITYTAG = "BlockEntityTag";

    /**
     * Returns true if less than 1256 MB in memory is allocated
     * to Minecraft.
     */
    public static boolean isLowMemoryMode() {
        if(memory==-1)
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
        IFluidState ret = Fluid.STATE_REGISTRY.getByValue(fluidStateId);
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
        return new Color((colourStateId << 2 >>> 2)*4 + colourStateId << 8 >>> 8, true);
    }

    /**
     * Get a blockstate's id.
     */
    public static int getStateId(final BlockState state) {
        return 0x00000003 << 30 + Math.max(0, Block.BLOCK_STATE_IDS.get(state));
    }

    /**
     * Get a fluidstate's id.
     */
    public static int getFluidId(final IFluidState fluid) {
       return 0x00000001 << 30 + Math.max(0, Fluid.STATE_REGISTRY.get(fluid));
    }

    /**
     * Get a colour's id.
     * Only 1/4 of the alpha is stored, nobody will notice
     * the small alpha differences anyways.
     */
    public static int getColourId(final Color colour) {
        //We built our own int instead of getting the colour value because we do some trickery to the MSBs.
        return 0x00000002 << 30 + (colour.getAlpha()/4) << 24 + colour.getRed() << 16 + colour.getGreen() << 8 + colour.getBlue();
    }
}
