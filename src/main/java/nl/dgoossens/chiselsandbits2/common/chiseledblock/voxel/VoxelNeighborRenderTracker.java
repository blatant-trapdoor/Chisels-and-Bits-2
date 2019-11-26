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
    private final ModelRenderState sides = new ModelRenderState();
    private WeakReference<VoxelBlobStateReference> lastCenter;
    private ModelRenderState lrs = null;
    private boolean invalid;
    private boolean rendered;

    public VoxelNeighborRenderTracker(IBlockReader world, BlockPos pos) {
        update(world, pos);
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
                    update(world, pos);
                    return;
                }
            } else {
                if(sides.get(d) != null) {
                    invalidate();
                    sides.remove(d);
                    return;
                }
            }
        }
    }

    //Flag this render tracker as having been rendered at least once, before this point invalidation is ignored to prevent invalidation spam
    public void setRendered() {
        rendered = true;
    }

    public void invalidate() {
        if(!rendered) return;
        invalid = true;
    }

    public boolean isValid() {
        if(invalid) {
            invalid = false;
            return false;
        }
        return true;
    }

    public ModelRenderState getRenderState(final VoxelBlobStateReference data) {
        if (lrs == null || lastCenter == null || lastCenter.get() != data) {
            lrs = new ModelRenderState(sides);
            lastCenter = new WeakReference<>(data);
        }
        return lrs;
    }
}
