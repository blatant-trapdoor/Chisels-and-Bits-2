package nl.dgoossens.chiselsandbits2.client.render.models.helpers;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class ModelLightMapReader extends BaseModelReader
{
	public int lv = 0;
	final float maxLightmap = 32.0f / 0xffff;
	private VertexFormat format = DefaultVertexFormats.BLOCK;
	boolean hasLightMap = false;

	public ModelLightMapReader()
	{
	}

	public void setVertexFormat(
			VertexFormat format )
	{
		hasLightMap = false;

		int eCount = format.getElementCount();
		for ( int x = 0; x < eCount; x++ )
		{
			VertexFormatElement e = format.getElement( x );
			if ( e.getUsage() == VertexFormatElement.Usage.UV && e.getIndex() == 1 && e.getType() == VertexFormatElement.Type.SHORT )
			{
				hasLightMap = true;
			}
		}

		this.format = format;
	}

	@Override
	public VertexFormat getVertexFormat()
	{
		return format;
	}

	@Override
	public void put(
			final int element,
			final float... data )
	{
		final VertexFormatElement e = getVertexFormat().getElement( element );

		if ( e.getUsage() == VertexFormatElement.Usage.COLOR.UV && e.getIndex() == 1 && e.getType() == VertexFormatElement.Type.SHORT && data.length >= 2 && hasLightMap )
		{
			final int lvFromData_sky = (int) ( data[0] / maxLightmap ) & 0xf;
			final int lvFromData_block = (int) ( data[1] / maxLightmap ) & 0xf;

			lv = Math.max( lvFromData_sky, lv );
			lv = Math.max( lvFromData_block, lv );
		}
	}

}