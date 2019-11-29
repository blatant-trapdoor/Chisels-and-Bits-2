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

    public VoxelNeighborRenderTracker(IBlockReader world, BlockPos pos) {
        update(world, pos);
    }

    /**
     * Updates the tracked neighbouring voxel references.
     */
    public void update(IBlockReader world, BlockPos pos) {
        if(world == null || pos == null) return; //No point in updating if we have no world/pos.
        final TileEntity me = world.getTileEntity(pos);
        if(!(me instanceof ChiseledBlockTileEntity)) return;
        final ChiseledBlockTileEntity cme = (ChiseledBlockTileEntity) me;

        for (Direction d : Direction.values()) {
            if(sides.has(d)) continue; //Only update direction if we don't have it already

            final TileEntity te = world.getTileEntity(pos.offset(d));
            if (te instanceof ChiseledBlockTileEntity) {
                ((ChiseledBlockTileEntity) te).getChunk(world); //Make sure to get the chunk so it gets registered.
                sides.put(d, ((ChiseledBlockTileEntity) te).getVoxelReference());

                //Put me in the other block to avoid circle updates
                if(((ChiseledBlockTileEntity) te).hasRenderTracker()) {
                    VoxelNeighborRenderTracker rTracker = ((ChiseledBlockTileEntity) te).getRenderTracker();
                    if(rTracker.sides.has(d.getOpposite())) continue; //If they have us, it's good
                    sides.put(d.getOpposite(), cme.getVoxelReference());
                }
            }
        }
    }

    //Checks all neighbours and sees if any have changed to no longer be a chiseled block.
    public boolean validate(final IBlockReader world, final BlockPos pos) {
        synchronized (sides) {
            for (Direction d : Direction.values()) {
                final TileEntity te = world.getTileEntity(pos.offset(d));
                if(te instanceof ChiseledBlockTileEntity)
                    sides.put(d, ((ChiseledBlockTileEntity) te).getVoxelReference());
                else
                    sides.remove(d);
            }
            return sides.queryDirty();
        }
    }

    public void invalidate() {
        sides.invalidate();
    }

    public boolean isValid() {
        return !sides.isDirty();
    }

    public ModelRenderState getRenderState(final VoxelBlobStateReference data) {
        if (lrs == null || lastCenter == null || lastCenter.get() != data) {
            lrs = new ModelRenderState(sides);
            lastCenter = new WeakReference<>(data);
        }
        return lrs;
    }
}
