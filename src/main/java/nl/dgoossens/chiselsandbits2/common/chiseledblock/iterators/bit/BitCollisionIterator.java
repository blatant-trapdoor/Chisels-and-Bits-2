package nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.bit;

import net.minecraft.util.math.AxisAlignedBB;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;

public class BitCollisionIterator extends BitIterator {
    public final static float one16th = 1.0f / VoxelBlob.DIMENSION;
    public final static AxisAlignedBB[] cachedBoxes = new AxisAlignedBB[VoxelBlob.DIMENSION3];

    public float physicalX;
    public float physicalY;
    public float physicalZ;

    public float physicalYp1 = one16th;
    public float physicalZp1 = one16th;

    @Override
    public boolean hasNext() {
        final boolean r = super.hasNext();
        physicalX = x * one16th;
        return r;
    }

    @Override
    protected void yPlus() {
        super.yPlus();
        physicalY = y * one16th;
        physicalYp1 = physicalY + one16th;
    }

    @Override
    protected void zPlus() {
        super.zPlus();

        physicalZ = z * one16th;
        physicalZp1 = physicalZ + one16th;

        physicalY = y * one16th;
        physicalYp1 = physicalY + one16th;
    }

    public AxisAlignedBB getBoundingBox() {
        AxisAlignedBB box = cachedBoxes[bit];

        if (box == null)
            box = cachedBoxes[bit] = new AxisAlignedBB(this.physicalX, this.physicalY, this.physicalZ, this.physicalX + BitCollisionIterator.one16th, this.physicalYp1, this.physicalZp1);

        return box;
    }

}
