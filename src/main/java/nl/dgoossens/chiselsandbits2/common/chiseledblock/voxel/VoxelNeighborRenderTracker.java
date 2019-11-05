package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IStateRef;
import nl.dgoossens.chiselsandbits2.client.render.ModelRenderState;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

import java.lang.ref.WeakReference;

/**
 * An object which wraps the ModelRenderState which handles
 * storing references to the states of neighbouring blocks.
 */
public final class VoxelNeighborRenderTracker {
    static final int IS_DYNAMIC = 1;
    static final int IS_LOCKED = 2;
    static final int IS_STATIC = 4;
    private final ModelRenderState sides = new ModelRenderState();
    private WeakReference<VoxelBlobStateReference> lastCenter;
    private ModelRenderState lrs = null;
    private byte isDynamic;
    private int faceCount;
    private boolean invalid;

    public VoxelNeighborRenderTracker(IBlockReader world, BlockPos pos) {
        isDynamic = IS_DYNAMIC | IS_LOCKED;
        faceCount = ChiselsAndBits2.getInstance().getConfig().dynamicModelFaceCount.get();
        update(world, pos);
    }

    public void unlockDynamic() {
        isDynamic = (byte) (isDynamic & ~IS_LOCKED);
    }

    /**
     * Updates the tracked neighbouring voxel references.
     */
    public void update(IBlockReader world, BlockPos pos) {
        if(world == null || pos == null) return; //No point in updating if we have no world/pos.
        for (Direction d : Direction.values()) {
            final TileEntity te = world.getTileEntity(pos.offset(d));
            if (te instanceof ChiseledBlockTileEntity) {
                ((ChiseledBlockTileEntity) te).getChunk(world); //Make sure to get the chunk so it gets registered.
                synchronized (this) {
                    sides.put(d, ((ChiseledBlockTileEntity) te).getVoxelReference());
                }
            }
        }
    }

    //Checks all neighbours and sees if any have changed to no longer be a chiseled block.
    public void validate(final IBlockReader world, final BlockPos pos) {
        for (Direction d : Direction.values()) {
            final TileEntity te = world.getTileEntity(pos.offset(d));
            if(te instanceof ChiseledBlockTileEntity) {
                if(sides.get(d) == null) {
                    invalidate();
                    return;
                }
            } else {
                if(sides.get(d) != null) {
                    invalidate();
                    return;
                }
            }
        }
    }

    public void invalidate() {
        invalid = true;
    }

    public boolean isValid() {
        if(invalid) {
            invalid = false;
            return false;
        }
        return true;
    }

    public void setFaceCount(final int fc) {
        faceCount = fc;
    }

    public boolean isDynamic() {
        return (isDynamic & IS_DYNAMIC) != 0;
    }

    public ModelRenderState getRenderState(final VoxelBlobStateReference data) {
        if (lrs == null || lastCenter == null || lastCenter.get() != data) {
            lrs = new ModelRenderState(sides);
            lastCenter = new WeakReference<>(data);
        }
        return lrs;
    }
}
