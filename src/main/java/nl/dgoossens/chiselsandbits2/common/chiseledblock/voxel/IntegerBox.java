package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;

public final class IntegerBox {
    public static final IntegerBox NULL = new IntegerBox(0, 0, 0, 0, 0, 0);

    public int minX, minY, minZ, maxX, maxY, maxZ;

    public IntegerBox(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
        minX = x1;
        maxX = x2;
        minY = y1;
        maxY = y2;
        minZ = z1;
        maxZ = z2;
    }

    public AxisAlignedBB toBoundingBox() {
        return new AxisAlignedBB(minX / 16.0d, minY / 16.0d, minZ / 16.0d, (maxX + 1) / 16.0d, (maxY + 1) / 16.0d, (maxZ + 1) / 16.0d);
    }

    public void move(final Direction side, final int scale) {
        minX += side.getXOffset() * scale;
        maxX += side.getXOffset() * scale;
        minY += side.getYOffset() * scale;
        maxY += side.getYOffset() * scale;
        minZ += side.getZOffset() * scale;
        maxZ += side.getZOffset() * scale;
    }

    public int width() {
        return maxX - minX;
    }

    public int height() {
        return maxY - minY;
    }

    public int depth() {
        return maxZ - minZ;
    }

    public boolean extendsOutsideAllowedBB() {
        return minX < 0 || minY < 0 || minZ < 0 || maxX >= 16 || maxY >= 16 || maxZ >= 16;
    }
}
