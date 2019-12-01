package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockSmartModel;
import nl.dgoossens.chiselsandbits2.client.render.ModelRenderState;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

import java.lang.ref.WeakReference;

/**
 * An object which wraps the ModelRenderState which handles
 * storing references to the states of neighbouring blocks.
 */
public final class VoxelNeighborRenderTracker {
    private final ModelRenderState sides = new ModelRenderState();

    private static int lastId = 0;
    private int id;

    public VoxelNeighborRenderTracker(World world, BlockPos pos) {
        id = lastId++;
        update(world, pos);
    }

    /**
     * Updates the tracked neighbouring voxel references.
     */
    public void update(World world, BlockPos pos) {
        if (world == null || pos == null) return; //No point in updating if we have no world/pos.
        final TileEntity me = world.getTileEntity(pos);
        if (!(me instanceof ChiseledBlockTileEntity)) return;
        final ChiseledBlockTileEntity cme = (ChiseledBlockTileEntity) me;

        for (Direction d : Direction.values()) {
            if (sides.has(d)) continue; //Only update direction if we don't have it already

            final TileEntity te = world.getTileEntity(pos.offset(d));
            if (te instanceof ChiseledBlockTileEntity) {
                System.out.println(id+" | We found a neighbour ["+pos+"] on side "+d);
                ((ChiseledBlockTileEntity) te).getChunk(world); //Make sure to get the chunk so it gets registered.
                sides.put(d, ((ChiseledBlockTileEntity) te).getVoxelReference());

                //Put me in the other block to avoid circle updates
                if (((ChiseledBlockTileEntity) te).hasRenderTracker()) {
                    VoxelNeighborRenderTracker rTracker = ((ChiseledBlockTileEntity) te).getRenderTracker();
                    if (rTracker.sides.has(d.getOpposite())) continue; //If they have us, it's good
                    System.out.println(id+" | We're helping our neighbour ["+pos+"] by setting side "+d.getOpposite()+" to us");
                    rTracker.sides.put(d.getOpposite(), cme.getVoxelReference());
                }
            }
        }
    }

    //Checks all neighbours and sees if any have changed to no longer be a chiseled block.
    public boolean validate(final World world, final BlockPos pos) {
        final TileEntity me = world.getTileEntity(pos);
        if (me instanceof ChiseledBlockTileEntity) {
            if(this != ((ChiseledBlockTileEntity) me).getRenderTracker())
                throw new RuntimeException("Validate was called on block that was not itself.");

            for (Direction d : Direction.values()) {
                final TileEntity te = world.getTileEntity(pos.offset(d));
                if (te instanceof ChiseledBlockTileEntity) {
                    if(sides.get(d) != ((ChiseledBlockTileEntity) te).getVoxelReference()) {
                        System.out.println(id+" | Found te adjacent to "+pos+" on side "+d+", old: "+sides.get(d));
                        sides.put(d, ((ChiseledBlockTileEntity) te).getVoxelReference());
                        for(Direction d2 : Direction.values()) {
                            if(sides.has(d2)) System.out.println(id+" | [RECAP] We have something on side "+d2+", namely: "+sides.get(d2));
                        }
                        continue;
                    }
                    sides.put(d, ((ChiseledBlockTileEntity) te).getVoxelReference());
                } else {
                    if(sides.has(d)) {
                        System.out.println(id+" | Removing adjacent block on "+pos+" in direction "+d+" from tracked neighbours, was: "+sides.get(d)+" because there's "+world.getBlockState(pos.offset(d)).getBlock()+" over there");
                        sides.remove(d);
                        for(Direction d2 : Direction.values()) {
                            if(sides.has(d2))
                                System.out.println(id+" | [RECAP] We have something on side "+d2+", namely: "+sides.get(d2));
                        }
                        continue;
                    }
                    sides.remove(d);
                }
            }

            //Validate the model cache right here to avoid this validation returning true time after time.
            return ChiseledBlockSmartModel.validate(this, sides);
        } else throw new RuntimeException("Validate was called on block that was not even a Chiseled Block");
    }

    public void invalidate() {
        sides.invalidate();
    }

    public boolean isValid() {
        return !sides.isDirty();
    }

    public ModelRenderState getRenderState() {
        return sides;
    }
}
