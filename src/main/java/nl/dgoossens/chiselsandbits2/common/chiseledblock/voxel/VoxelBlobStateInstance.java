package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.zip.InflaterInputStream;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

public final class VoxelBlobStateInstance implements Comparable<VoxelBlobStateInstance>
{

	public final int hash;
	public final byte[] voxelBytes;

	private static final int HAS_FLUIDS = 1;
	private static final int HAS_SOLIDS = 2;

	// Separate fluids and solids, and use both for occlusion.
	private int generated = 0;
	private SoftReference<AxisAlignedBB[]> fluidBoxes = null;
	private SoftReference<AxisAlignedBB[]> solidBoxes = null;

	protected SoftReference<VoxelBlob> blob;

	public VoxelBlobStateInstance(
			final byte[] data )
	{
		voxelBytes = data;
		hash = Arrays.hashCode( voxelBytes );
	}

	@Override
	public boolean equals(
			final Object obj )
	{
		return compareTo( (VoxelBlobStateInstance) obj ) == 0;
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public int compareTo(
			final VoxelBlobStateInstance o )
	{
		if ( o == null )
		{
			return -1;
		}

		int r = Integer.compare( hash, o.hash );

		// length?
		if ( r == 0 )
		{
			r = voxelBytes.length - o.voxelBytes.length;
		}

		// for real then...
		if ( r == 0 )
		{
			for ( int x = 0; x < voxelBytes.length && r == 0; x++ )
			{
				r = voxelBytes[x] - o.voxelBytes[x];
			}
		}

		return r;
	}

	public VoxelBlob getBlob()
	{
		try
		{
			return getBlobCatchable();
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
			VoxelBlob vb = new VoxelBlob();
			vb.fill(ModUtil.getStateId(Blocks.STONE.getDefaultState())); //Fill with stone by default.
			return vb;
		}
	}

	public VoxelBlob getBlobCatchable() throws Exception
	{
		VoxelBlob vb = blob == null ? null : blob.get();

		if ( vb == null )
		{
			vb = new VoxelBlob();
			vb.blobFromBytes( voxelBytes );
			blob = new SoftReference<>( vb );
		}

		return new VoxelBlob( vb );
	}

	/*private AxisAlignedBB[] getBoxType(
			final int type )
	{
		// if they are not generated, then generate them.
		if ( ( generated & type ) == 0 )
		{
			switch ( type )
			{
				//TODO fluids
				*case HAS_FLUIDS:

					final VoxelBlob fluidBlob = getVoxelReference();
					generated |= HAS_FLUIDS;

					if ( fluidBlob.filterFluids( true ) )
					{
						final AxisAlignedBB[] out = generateBoxes( fluidBlob );
						fluidBoxes = new SoftReference<>( out );
						return out;
					}

					fluidBoxes = null;
					return null;
				*
				case HAS_SOLIDS:

					final VoxelBlob solidBlob = getBlob();
					generated |= HAS_SOLIDS;

					//TODO if ( solidBlob.filterFluids( false ) )
					{
						final AxisAlignedBB[] out = generateBoxes( solidBlob );
						solidBoxes = new SoftReference<>( out );
						return out;
					}

					//solidBoxes = null;
					//return null;

			}

		}

		// snag the boxes we want.
		AxisAlignedBB[] out = null;
		switch ( type )
		{
			case HAS_FLUIDS:

				if ( fluidBoxes == null )
				{
					return null;
				}

				out = fluidBoxes.fromName();
				break;

			case HAS_SOLIDS:

				if ( solidBoxes == null )
				{
					return null;
				}

				out = solidBoxes.fromName();
				break;
		}

		// did they expire?
		if ( out != null )
		{
			return out;
		}

		// regenerate boxes...
		generated = generated & ~type;
		return getBoxType( type );
	}*/

	public Collection<AxisAlignedBB> getBoxes() {
		return Arrays.asList(generateBoxes(getBlob()));
	}

	private AxisAlignedBB[] generateBoxes(
			final VoxelBlob blob )
	{
		final List<AxisAlignedBB> cache = new ArrayList<AxisAlignedBB>();
		final BitOcclusionIterator boi = new BitOcclusionIterator( cache );

		while ( boi.hasNext() )
		{
			if ( boi.getNext( blob ) != 0 )
			{
				boi.add();
			}
			else
			{
				boi.drop();
			}
		}

		return cache.toArray( new AxisAlignedBB[cache.size()] );
	}

	// cache the format after reading it once.
	private int format = Integer.MIN_VALUE;

	public int getFormat()
	{
		if ( format == Integer.MIN_VALUE )
		{
			if ( voxelBytes == null || voxelBytes.length == 0 )
			{
				format = -1;
			}
			else
			{
				try
				{
					final InflaterInputStream arrayPeek = new InflaterInputStream( new ByteArrayInputStream( voxelBytes ) );
					final byte[] peekBytes = new byte[5];
					arrayPeek.read( peekBytes );

					final PacketBuffer header = new PacketBuffer( Unpooled.wrappedBuffer( peekBytes ) );
					format = header.readVarInt();
				}
				catch ( final IOException e )
				{
					format = 0;
				}
			}
		}

		return format;
	}
}
