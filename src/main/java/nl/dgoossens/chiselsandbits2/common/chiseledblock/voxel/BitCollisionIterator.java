package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import net.minecraft.util.math.AxisAlignedBB;

public class BitCollisionIterator extends BitIterator {

    public final static float One16thf = 1.0f / VoxelBlob.DIMENSION;
    public final static AxisAlignedBB[] cachedBoxes = new AxisAlignedBB[VoxelBlob.DIMENSION3];

    public float physicalX;
    public float physicalY;
    public float physicalZ;

    public float physicalYp1 = One16thf;
    public float physicalZp1 = One16thf;

    @Override
    public boolean hasNext() {
        final boolean r = super.hasNext();
        physicalX = x * One16thf;
        return r;
    }

    @Override
    protected void yPlus() {
        super.yPlus();
        physicalY = y * One16thf;
        physicalYp1 = physicalY + One16thf;
    }

    @Override
    protected void zPlus() {
        super.zPlus();

        physicalZ = z * One16thf;
        physicalZp1 = physicalZ + One16thf;

        physicalY = y * One16thf;
        physicalYp1 = physicalY + One16thf;
    }

    public AxisAlignedBB getBoundingBox() {
        AxisAlignedBB box = cachedBoxes[bit];

        if (box == null)
            box = cachedBoxes[bit] = new AxisAlignedBB(this.physicalX, this.physicalY, this.physicalZ, this.physicalX + BitCollisionIterator.One16thf, this.physicalYp1, this.physicalZp1);

        return box;
    }

}
