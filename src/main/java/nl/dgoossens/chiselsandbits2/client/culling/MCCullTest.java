package nl.dgoossens.chiselsandbits2.client.culling;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import nl.dgoossens.chiselsandbits2.api.ICullTest;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

/**
 * Determine Culling using Block's Native Check.
 */
public class MCCullTest implements ICullTest {
	@Override
	public boolean isVisible(final int myId, final int otherId) {
		//If this is air
		if(myId == 0 || myId == otherId) return false;

		switch(VoxelType.getType(myId)) {
			case FLUIDSTATE:
			case AIR:
				return true;
			case COLOURED:
				return false;
			case BLOCKSTATE:
			{
				BlockState a = ModUtil.getBlockState(myId);
				BlockState b = VoxelType.getType(otherId) != VoxelType.BLOCKSTATE ? Blocks.AIR.getDefaultState() : ModUtil.getBlockState(otherId);

				//If we're stained glass but the one behind us too we don't need to show it. (hardcoded because it looks horrible otherwise)
				if (a.getBlock() instanceof StainedGlassBlock && a.getBlock() == b.getBlock()) return false;

				try {
					DummyEnvironmentWorldReader dummyWorld = new DummyEnvironmentWorldReader() {
						@Override
						public BlockState getBlockState(BlockPos pos) {
							if(pos.equals(BlockPos.ZERO)) return a;
							return super.getBlockState(pos);
						}
					};
					return a.doesSideBlockRendering(dummyWorld, BlockPos.ZERO, Direction.NORTH);
				} catch (Exception t) {}
			}
		}

		//Backup logic in case any errors occur
		return new SolidCullTest().isVisible(myId, otherId);
	}
}
