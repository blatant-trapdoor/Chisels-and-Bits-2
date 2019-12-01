package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.block.BitAccess;
import nl.dgoossens.chiselsandbits2.api.block.IVoxelSrc;

import java.util.Optional;

public class VoxelRegionSrc implements IVoxelSrc {
    final BlockPos min;
    final BlockPos max;
    final BlockPos actingCenter;

    final int wrapZ;
    final int wrapY;
    final int wrapX;

    final VoxelBlob blobs[];

    private VoxelRegionSrc(final World src, final BlockPos min, final BlockPos max, final BlockPos actingCenter) {
        this.min = min;
        this.max = max;
        this.actingCenter = actingCenter.subtract(min);

        wrapX = max.getX() - min.getX() + 1;
        wrapY = max.getY() - min.getY() + 1;
        wrapZ = max.getZ() - min.getZ() + 1;

        blobs = new VoxelBlob[wrapX * wrapY * wrapZ];

        for (int x = min.getX(); x <= max.getX(); ++x) {
            for (int y = min.getY(); y <= max.getY(); ++y) {
                for (int z = min.getZ(); z <= max.getZ(); ++z) {
                    final int idx = x - min.getX() + (y - min.getY()) * wrapX + (z - min.getZ()) * wrapX * wrapY;
                    final Optional<BitAccess> access = ChiselsAndBits2.getInstance().getAPI().getBitAccess(src, new BlockPos(x, y, z));
                    if (access.isPresent()) blobs[idx] = access.get().getNativeBlob();
                    else blobs[idx] = new VoxelBlob();
                }
            }
        }
    }

    public VoxelRegionSrc(final World theWorld, final BlockPos blockPos, final int range) {
        this(theWorld, blockPos.add(-range, -range, -range), blockPos.add(range, range, range), blockPos);
    }

    @Override
    public int getSafe(int x, int y, int z) {
        x += actingCenter.getX() * VoxelBlob.DIMENSION;
        y += actingCenter.getY() * VoxelBlob.DIMENSION;
        z += actingCenter.getZ() * VoxelBlob.DIMENSION;

        final int bitPosX = x & 0xf;
        final int bitPosY = y & 0xf;
        final int bitPosZ = z & 0xf;

        final int blkPosX = x >> 4;
        final int blkPosY = y >> 4;
        final int blkPosZ = z >> 4;

        final int idx = blkPosX + blkPosY * wrapX + blkPosZ * wrapX * wrapY;
        if (blkPosX < 0 || blkPosY < 0 || blkPosZ < 0 || blkPosX >= wrapX || blkPosY >= wrapY || blkPosZ >= wrapZ)
            return 0;
        return blobs[idx].get(bitPosX, bitPosY, bitPosZ);
    }

    public VoxelBlob getBlobAt(final BlockPos blockPos) {
        final int blkPosX = blockPos.getX() - min.getX();
        final int blkPosY = blockPos.getY() - min.getY();
        final int blkPosZ = blockPos.getZ() - min.getZ();

        final int idx = blkPosX + blkPosY * wrapX + blkPosZ * wrapX * wrapY;
        if (blkPosX < 0 || blkPosY < 0 || blkPosZ < 0 || blkPosX >= wrapX || blkPosY >= wrapY || blkPosZ >= wrapZ)
            return new VoxelBlob();
        return blobs[idx];
    }
}
