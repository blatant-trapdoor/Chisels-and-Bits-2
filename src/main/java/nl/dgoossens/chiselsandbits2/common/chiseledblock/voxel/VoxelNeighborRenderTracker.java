package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import java.lang.ref.WeakReference;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IStateRef;
import nl.dgoossens.chiselsandbits2.client.render.ModelRenderState;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

/**
 * An object which wraps the ModelRenderState which handles
 * storing references to the states of neighbouring blocks.
 */
public final class VoxelNeighborRenderTracker {
	static final int IS_DYNAMIC = 1;
	static final int IS_LOCKED = 2;
	static final int IS_STATIC = 4;

	private WeakReference<VoxelBlobStateReference> lastCenter;
	private ModelRenderState lrs = null;
	private final ModelRenderState sides = new ModelRenderState();

	private byte isDynamic;
	private int faceCount;

	public VoxelNeighborRenderTracker() {
		isDynamic = IS_DYNAMIC | IS_LOCKED;
		faceCount = ChiselsAndBits2.getConfig().dynamicModelFaceCount.get();
	}

	public void unlockDynamic() {
		isDynamic = (byte) ( isDynamic & ~IS_LOCKED );
	}

	/**
	 * Updates the tracked neighbouring voxel references.
	 */
	public void update(IBlockReader world, BlockPos pos) {
		System.out.println("Updating NEIGHBOURS "+pos);
		for(Direction d : Direction.values()) {
			final TileEntity te = world.getTileEntity(pos.offset(d));
			if(te instanceof ChiseledBlockTileEntity) {
				((ChiseledBlockTileEntity) te).getChunk(world); //Make sure to get the chunk so it gets registered.
				synchronized(this) {
					sides.put(d, ((ChiseledBlockTileEntity) te).getVoxelReference());
				}
			}
		}
	}

	public void setFaceCount(final int fc) {
		faceCount = fc;
	}

	public boolean isDynamic() {
		return (isDynamic & IS_DYNAMIC) != 0;
	}

	public ModelRenderState getRenderState(final VoxelBlobStateReference data) {
		if(lrs == null || lastCenter == null || lastCenter.get() != data) {
			lrs = new ModelRenderState(sides);
			lastCenter = new WeakReference<>(data);
		}
		return lrs;
	}
}
