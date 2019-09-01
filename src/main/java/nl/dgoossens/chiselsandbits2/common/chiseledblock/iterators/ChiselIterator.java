package nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.IntegerBox;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;

public interface ChiselIterator {
	IntegerBox getVoxelBox(
            VoxelBlob blobAt,
            boolean b);
	AxisAlignedBB getBoundingBox(
            VoxelBlob NULL_BLOB,
			boolean b);
	Direction side();

	int x();
	int y();
	int z();
	boolean hasNext();
}
