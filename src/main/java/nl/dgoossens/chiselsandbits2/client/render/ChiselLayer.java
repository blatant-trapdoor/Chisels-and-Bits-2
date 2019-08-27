package nl.dgoossens.chiselsandbits2.client.render;

import net.minecraft.util.BlockRenderLayer;
import nl.dgoossens.chiselsandbits2.api.ICullTest;
import nl.dgoossens.chiselsandbits2.client.culling.MCCullTest;
import nl.dgoossens.chiselsandbits2.api.VoxelType;

import java.security.InvalidParameterException;

public enum ChiselLayer {
	SOLID(BlockRenderLayer.SOLID, VoxelType.SOLID_BLOCKSTATE),
	SOLID_FLUID(BlockRenderLayer.SOLID, VoxelType.FLUIDSTATE),
	CUTOUT(BlockRenderLayer.CUTOUT, null),
	CUTOUT_MIPPED(BlockRenderLayer.CUTOUT_MIPPED, null),
	TRANSLUCENT(BlockRenderLayer.TRANSLUCENT, null);

	public final BlockRenderLayer layer;
	public final VoxelType type;

	ChiselLayer(final BlockRenderLayer layer, final VoxelType type) {
		this.layer = layer; this.type = type;
	}

	/*public boolean filter(
			final VoxelBlob vb )
	{
		if ( vb == null )
		{
			return false;
		}

		if ( vb.filter( layer ) )
		{
			//TODO if ( type != null )
			//{
			//	return vb.filterFluids( type == VoxelType.FLUIDSTATE );
			//}

			return true;
		}
		return false;
	}*/

	public static ChiselLayer fromLayer(
			final BlockRenderLayer layerInfo,
			final boolean isFluid )
	{
		switch ( layerInfo )
		{
			case CUTOUT:
				return CUTOUT;
			case CUTOUT_MIPPED:
				return CUTOUT_MIPPED;
			case SOLID:
				return isFluid ? SOLID_FLUID : SOLID;
			case TRANSLUCENT:
				return TRANSLUCENT;
		}

		throw new InvalidParameterException();
	}

	/**
	 * Get the ICullTest this layer uses.
	 */
	public ICullTest getTest() {
		return new MCCullTest();
	}
}
