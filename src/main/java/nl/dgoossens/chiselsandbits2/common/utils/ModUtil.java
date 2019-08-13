package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Random;

public class ModUtil {
    @Nonnull
    public static final String NBT_BLOCKENTITYTAG = "BlockEntityTag";

    /**
     * Get the blockstate corresponding to an id.
     */
    public static BlockState getStateById(final int blockStateID) {
        return Block.getStateById(blockStateID);
    }

    /**
     * Get a blockstate's id.
     */
    public static int getStateId(final BlockState state) {
        return Math.max(0, Block.getStateId(state));
    }

    //The amount of memory allocated to Minecraft.
    private static long memory = -1;

    /**
     * Returns true if less than 1256 MB in memory is allocated
     * to Minecraft.
     */
    public static boolean isLowMemoryMode() {
        if(memory==-1)
            memory = Runtime.getRuntime().maxMemory() / (1024 * 1024); // mb
        return memory < 1256;
    }
}
