package nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators;

import net.minecraft.util.math.AxisAlignedBB;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.IntegerBox;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;

public abstract class BaseChiselIterator implements ChiselIterator
{

	@Override
	public IntegerBox getVoxelBox(
			final VoxelBlob vb,
			final boolean boundSolids )
	{
		final IntegerBox box = new IntegerBox( 0, 0, 0, 0, 0, 0 );

		boolean started = false;
		while (hasNext()) {
			if (vb.get( x(), y(), z() ) != 0 == boundSolids) {
				if (started) {
					box.minX = Math.min(box.minX, x());
					box.minY = Math.min(box.minY, y());
					box.minZ = Math.min(box.minZ, z());
					box.maxX = Math.max(box.maxX, x());
					box.maxY = Math.max(box.maxY, y());
					box.maxZ = Math.max(box.maxZ, z());
				} else {
					started = true;
					box.minX = x();
					box.minY = y();
					box.minZ = z();
					box.maxX = x();
					box.maxY = y();
					box.maxZ = z();
				}
			}
		}

		return started ? box : null;
	}

	@Override
	public AxisAlignedBB getBoundingBox(final VoxelBlob NULL_BLOB, final boolean boundSolids) {
		final float One16thf = 1.0f / NULL_BLOB.DIMENSION;
		final IntegerBox box = getVoxelBox(NULL_BLOB, boundSolids);

		return box != null ?  new AxisAlignedBB( box.minX * One16thf, box.minY * One16thf, box.minZ * One16thf, ( box.maxX + 1 ) * One16thf, ( box.maxY + 1 ) * One16thf, ( box.maxZ + 1 ) * One16thf ) : null;
	}
}
