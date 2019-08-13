package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import java.lang.ref.WeakReference;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import nl.dgoossens.chiselsandbits2.api.IStateRef;
import nl.dgoossens.chiselsandbits2.client.render.ModelRenderState;

public final class VoxelNeighborRenderTracker
{
	static final int IS_DYNAMIC = 1;
	static final int IS_LOCKED = 2;
	static final int IS_STATIC = 4;

	private WeakReference<VoxelBlobStateReference> lastCenter;
	private ModelRenderState lrs = null;

	private byte isDynamic;
	Integer[] faceCount = new Integer[4];

	public void unlockDynamic()
	{
		isDynamic = (byte) ( isDynamic & ~IS_LOCKED );
	}

	public VoxelNeighborRenderTracker()
	{
		faceCount = new Integer[BlockRenderLayer.values().length];
		isDynamic = IS_DYNAMIC | IS_LOCKED;
		for ( int x = 0; x < faceCount.length; ++x )
		{
			faceCount[x] = 40 + 1; //TODO allow config here
		}
	}

	private final ModelRenderState sides = new ModelRenderState( null );

	public boolean isAboveLimit()
	{
		int faces = 0;

		for ( int x = 0; x < faceCount.length; ++x )
		{
			if ( faceCount[x] == null )
			{
				return false;
			}

			faces += faceCount[x];
		}

		return faces >= 40;
	}

	public void setAbovelimit(
			final BlockRenderLayer layer,
			final int fc )
	{
		faceCount[layer.ordinal()] = fc;
	}

	public boolean isDynamic()
	{
		return ( isDynamic & IS_DYNAMIC ) != 0;
	}

	private boolean sameValue(
			final IStateRef iStateRef,
			final IStateRef value )
	{
		if ( iStateRef == value )
		{
			return true;
		}

		if ( iStateRef == null || value == null )
		{
			return false;
		}

		return value.equals( iStateRef );
	}

	public ModelRenderState getRenderState(
			final VoxelBlobStateReference data )
	{
		if ( lrs == null || lastCenter == null )
		{
			lrs = new ModelRenderState( sides );
			updateCenter( data );
		}
		else if ( lastCenter.get() != data )
		{
			updateCenter( data );
			lrs = new ModelRenderState( sides );
		}

		return lrs;
	}

	private void updateCenter(final VoxelBlobStateReference data ) {
		lastCenter = new WeakReference<>( data );
	}
}
