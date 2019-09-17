package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import nl.dgoossens.chiselsandbits2.api.BitOperation;

public class BitLocation {
    private static final double ONE_32ND = 0.5 / VoxelBlob.DIMENSION;

    public final BlockPos blockPos;
    public final int bitX, bitY, bitZ;

    public BitLocation(final BlockRayTraceResult mop, final boolean absHit, final BitOperation type) {
        final BlockPos absOffset = absHit ? mop.getPos() : BlockPos.ZERO;
        final Vec3d crds = mop.getHitVec().subtract(absOffset.getX(), absOffset.getY(), absOffset.getZ())
                .subtract(new Vec3d(mop.getFace().getXOffset(), mop.getFace().getYOffset(), mop.getFace().getZOffset()).scale(ONE_32ND));
        if (type == BitOperation.PLACE) {
            blockPos = mop.getPos();

            bitX = snapToValid((int) Math.floor(crds.getX() * VoxelBlob.DIMENSION));
            bitY = snapToValid((int) Math.floor(crds.getY() * VoxelBlob.DIMENSION));
            bitZ = snapToValid((int) Math.floor(crds.getZ() * VoxelBlob.DIMENSION));
        } else {
            final int bitXi = (int) Math.floor(crds.getX() * VoxelBlob.DIMENSION);
            final int bitYi = (int) Math.floor(crds.getY() * VoxelBlob.DIMENSION);
            final int bitZi = (int) Math.floor(crds.getZ() * VoxelBlob.DIMENSION);

            if (bitXi < 0 || bitYi < 0 || bitZi < 0 || bitXi >= VoxelBlob.DIMENSION || bitYi >= VoxelBlob.DIMENSION || bitZi >= VoxelBlob.DIMENSION) {
                blockPos = mop.getPos().offset(mop.getFace());
                bitX = snapToValid(bitXi - mop.getFace().getXOffset() * VoxelBlob.DIMENSION);
                bitY = snapToValid(bitYi - mop.getFace().getYOffset() * VoxelBlob.DIMENSION);
                bitZ = snapToValid(bitZi - mop.getFace().getZOffset() * VoxelBlob.DIMENSION);
            } else {
                blockPos = mop.getPos();
                bitX = snapToValid(bitXi);
                bitY = snapToValid(bitYi);
                bitZ = snapToValid(bitZi);
            }
        }
    }

    public BitLocation(final BlockPos pos, final int x, final int y, final int z) {
        blockPos = pos;
        bitX = x;
        bitY = y;
        bitZ = z;
    }

    public static BitLocation min(final BitLocation from, final BitLocation to) {
        final int bitX = min(from.blockPos.getX(), to.blockPos.getX(), from.bitX, to.bitX);
        final int bitY = min(from.blockPos.getY(), to.blockPos.getY(), from.bitY, to.bitY);
        final int bitZ = min(from.blockPos.getZ(), to.blockPos.getZ(), from.bitZ, to.bitZ);

        return new BitLocation(new BlockPos(
                Math.min(from.blockPos.getX(), to.blockPos.getX()),
                Math.min(from.blockPos.getY(), to.blockPos.getY()),
                Math.min(from.blockPos.getZ(), to.blockPos.getZ())),
                bitX, bitY, bitZ);
    }

    public static BitLocation max(final BitLocation from, final BitLocation to) {
        final int bitX = max(from.blockPos.getX(), to.blockPos.getX(), from.bitX, to.bitX);
        final int bitY = max(from.blockPos.getY(), to.blockPos.getY(), from.bitY, to.bitY);
        final int bitZ = max(from.blockPos.getZ(), to.blockPos.getZ(), from.bitZ, to.bitZ);

        return new BitLocation(new BlockPos(
                Math.max(from.blockPos.getX(), to.blockPos.getX()),
                Math.max(from.blockPos.getY(), to.blockPos.getY()),
                Math.max(from.blockPos.getZ(), to.blockPos.getZ())),
                bitX, bitY, bitZ);
    }

    private static int min(final int x, final int x2, final int bitX2, final int bitX3) {
        if (x < x2) return bitX2;
        if (x2 == x) return Math.min(bitX2, bitX3);
        return bitX3;
    }

    private static int max(final int x, final int x2, final int bitX2, final int bitX3) {
        if (x > x2) return bitX2;
        if (x2 == x) return Math.max(bitX2, bitX3);
        return bitX3;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public int getBitX() {
        return bitX;
    }

    public int getBitY() {
        return bitY;
    }

    public int getBitZ() {
        return bitZ;
    }

    public int snapToValid(final int x) {
        // rounding can sometimes create -1 or 16, just snap int to the nearest
        // valid position and move on.
        return Math.min(Math.max(0, x), 15);
    }
}
