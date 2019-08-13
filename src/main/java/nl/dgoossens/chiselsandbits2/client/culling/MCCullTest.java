package nl.dgoossens.chiselsandbits2.client.culling;

import net.minecraft.block.BlockState;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import nl.dgoossens.chiselsandbits2.api.ICullTest;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

/**
 * Determine Culling using Block's Native Check.
 */
public class MCCullTest implements ICullTest {
	@Override
	public boolean isVisible(final int mySpot, final int secondSpot ) {
		//If this is air
		if (mySpot == 0 || mySpot == secondSpot) return false;

		BlockState a = ModUtil.getStateById(mySpot);
		BlockState b = ModUtil.getStateById(secondSpot);

		//If we're stained glass but the one behind us too we don't need to show it. (hardcoded because it looks horrible otherwise)
		if (a.getBlock() instanceof StainedGlassBlock && a.getBlock() == b.getBlock() ) return false;

		try {
			DummyEnvironmentWorldReader dummyWorld = new DummyEnvironmentWorldReader() {
				@Override
				public BlockState getBlockState(BlockPos pos) {
					if(pos.equals(BlockPos.ZERO)) return a;
					return super.getBlockState(pos);
				}
			};
			return a.doesSideBlockRendering(dummyWorld, BlockPos.ZERO, Direction.NORTH);
		} catch (Exception t) {
			// revert to older logic in the event of some sort of issue.
			return new SolidCullTest().isVisible(mySpot, secondSpot);
		}
	}
}
