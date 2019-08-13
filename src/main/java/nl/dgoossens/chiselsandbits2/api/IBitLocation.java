package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.util.math.BlockPos;

public interface IBitLocation {
	/**
	 * Get block position that the bit is inside.
	 *
	 * @return Block Pos
	 */
	BlockPos getBlockPos();

	/**
	 * get Bit X coordinate.
	 *
	 * @return X coordinate
	 */
	int getBitX();

	/**
	 * get Bit Y coordinate.
	 *
	 * @return Y coordinate
	 */
	int getBitY();

	/**
	 * get Bit Z coordinate.
	 *
	 * @return Z coordinate
	 */
	int getBitZ();
}
