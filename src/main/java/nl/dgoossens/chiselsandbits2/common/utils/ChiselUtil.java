package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelType;

import java.util.concurrent.CopyOnWriteArraySet;

public class ChiselUtil {
    private static CopyOnWriteArraySet<Block> supportedBlocks = new CopyOnWriteArraySet<>();
    static {
        supportedBlocks.add(Blocks.GRASS_BLOCK);
        supportedBlocks.add(Blocks.DIRT);
        supportedBlocks.add(Blocks.COBBLESTONE);
        supportedBlocks.add(Blocks.STONE); //TODO temporary list of allowed blocks
    }
    private static CopyOnWriteArraySet<Integer> fluidStates = new CopyOnWriteArraySet<>();

    /**
     * Checks whether or not a given block can be chiselled.
     */
    public static boolean canChiselBlock(BlockState block) {
        if(block.getBlock() instanceof ChiseledBlock) return true; //TODO also if instance of little tiles or mc multipart block
        //TODO add test to see if a block can be chiselled and then add it to the list
        return supportedBlocks.contains(block.getBlock());
    }

    /**
     * Check if a block at a given position is already chiseled.
     */
    public static boolean isBlockChiseled(BlockPos position, IBlockReader world) {
        return world.getBlockState(position).getBlock() instanceof ChiseledBlock;
    }

    /**
     * Get the voxel type of an given blockstate's id.
     */
    public static VoxelType getTypeFromStateID(final int bit) {
        if(bit == 0) return VoxelType.AIR;
        return fluidStates.contains(bit) ? VoxelType.FLUID : VoxelType.SOLID;
    }
}
