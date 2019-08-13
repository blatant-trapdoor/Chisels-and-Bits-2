package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * Implemented by C&B Blocks, can be used to request a material that represents
 * the largest quantity of a C&B block.
 */
public interface IMultiStateBlock {
	/**
	 * Get the primary blockstate that this
	 * block is made of.
	 */
	BlockState getPrimaryState(
            IBlockReader world,
            BlockPos pos);
}
