package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.dgoossens.chiselsandbits2.api.ICullTest;
import nl.dgoossens.chiselsandbits2.api.IVoxelSrc;
import nl.dgoossens.chiselsandbits2.api.StateCount;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization.BitStream;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization.BlobSerilizationCache;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization.VoxelSerializer;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class VoxelBlob implements IVoxelSrc {
	public final static int DIMENSION = 16;
	public final static int DIMENSION2 = DIMENSION * DIMENSION;
	public final static int DIMENSION3 = DIMENSION2 * DIMENSION;

	public final static int ARRAY_SIZE = DIMENSION3;
	public final static int DIMENSION_MINUS_ONE = DIMENSION - 1;

	private final int[] values = new int[ARRAY_SIZE];


	//--- CONSTRUCTORS ---
	public VoxelBlob() {}
	protected VoxelBlob(final VoxelBlob vb) {
		for(int x = 0; x < values.length; ++x)
			values[x] = vb.values[x];
	}

	//--- MODIFICATION METHODS ---
	/**
	 * Merges the second voxelblob into this one.
	 * Returns this.
	 */
	public VoxelBlob merge(final VoxelBlob second) {
		for(int x = 0; x < values.length; ++x)
			if(values[x] == 0) values[x] = second.values[x];
		return this;
	}

	/**
	 * Returns a new VoxelBlob which contains
	 * a version of this one mirrored
	 * across the given axis.
	 */
	public VoxelBlob mirror(final Direction.Axis axis) {
		VoxelBlob out = new VoxelBlob();
		final BitIterator bi = new BitIterator();
		while(bi.hasNext()) {
			if(bi.getNext(this) != 0) {
				switch(axis) {
					case X:
						out.set(DIMENSION_MINUS_ONE - bi.x, bi.y, bi.z, bi.getNext(this));
						break;
					case Y:
						out.set(bi.x, DIMENSION_MINUS_ONE - bi.y, bi.z, bi.getNext(this));
						break;
					case Z:
						out.set(bi.x, bi.y, DIMENSION_MINUS_ONE - bi.z, bi.getNext(this));
						break;
				}
			}
		}
		return out;
	}

	//--- LOOKUP METHODS ---
	/**
	 * Will return true if for every bit in the voxelblob
	 * it is true that it is air in either or both of
	 * the blobs. (no bit is set in both)
	 */
	public boolean canMerge(final VoxelBlob second) {
		for(int x = 0; x < values.length; ++x)
			if(values[x] != 0 && second.values[x] != 0) return false;

		return true;
	}

	/**
	 * Get the position of the center of the shape.
	 */
	public BlockPos getCenter() {
		final IntegerBox bounds = getBounds();
		return bounds!=null ? new BlockPos( ( bounds.minX + bounds.maxX ) / 2, ( bounds.minY + bounds.maxY ) / 2, ( bounds.minZ + bounds.maxZ ) / 2 ) : null;
	}

	/**
	 * G
	 */
	public IntegerBox getBounds() {
		boolean found = false;
		int min_x = 0, min_y = 0, min_z = 0;
		int max_x = 0, max_y = 0, max_z = 0;

		final BitIterator bi = new BitIterator();
		while (bi.hasNext()) {
			if(bi.getNext(this) != 0) {
				if (found) {
					min_x = Math.min(min_x, bi.x);
					min_y = Math.min(min_y, bi.y);
					min_z = Math.min(min_z, bi.z);

					max_x = Math.max(max_x, bi.x);
					max_y = Math.max(max_y, bi.y);
					max_z = Math.max(max_z, bi.z);
				} else {
					found = true;

					min_x = bi.x;
					min_y = bi.y;
					min_z = bi.z;

					max_x = bi.x;
					max_y = bi.y;
					max_z = bi.z;
				}
			}
		}
		return found ? new IntegerBox( min_x, min_y, min_z, max_x, max_y, max_z ) : null;
	}

	public VoxelBlob spin(
			final Direction.Axis axis )
	{
		final VoxelBlob d = new VoxelBlob();

		/*
		 * Rotate by -90 Degrees: x' = y y' = - x
		 */

		final BitIterator bi = new BitIterator();
		while ( bi.hasNext() )
		{
			switch ( axis )
			{
				case X:
					d.set(bi.x, DIMENSION_MINUS_ONE - bi.z, bi.y, bi.getNext(this));
					break;
				case Y:
					d.set(bi.z, bi.y, DIMENSION_MINUS_ONE - bi.x, bi.getNext(this));
					break;
				case Z:
					d.set(DIMENSION_MINUS_ONE - bi.y, bi.x, bi.z, bi.getNext(this));
					break;
				default:
					throw new NullPointerException();
			}
		}

		return d;
	}

	public VoxelBlob fill(
			final int value )
	{
		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			values[x] = value;
		}
		return this;
	}

	public VoxelBlob fill(
			final VoxelBlob src )
	{
		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			values[x] = src.values[x];
		}
		return this;
	}

	public VoxelBlob clear()
	{
		fill( 0 );
		return this;
	}

	public int air()
	{
		int p = 0;

		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			if ( values[x] == 0 )
			{
				p++;
			}
		}

		return p;
	}

	public void binaryReplacement(
			final int airReplacement,
			final int solidReplacement )
	{
		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			values[x] = values[x] == 0 ? airReplacement : solidReplacement;
		}
	}

	public int filled()
	{
		int p = 0;

		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			if ( values[x] != 0 )
			{
				p++;
			}
		}

		return p;
	}

	protected int getBit(
			final int offset )
	{
		return values[offset];
	}

	protected void putBit(
			final int offset,
			final int newValue )
	{
		values[offset] = newValue;
	}

	public int get(
			final int x,
			final int y,
			final int z )
	{
		return getBit( x | y << 4 | z << 8 );
	}

	public VoxelType getVoxelType(
			final int x,
			final int y,
			final int z )
	{
		int i = get( x, y, z );
		return i==0 ? VoxelType.AIR : VoxelType.SOLID; //Add FLUID possibility
	}

	public void set(
			final int x,
			final int y,
			final int z,
			final int value )
	{
		putBit( x | y << 4 | z << 8, value );
	}

	public void clear(
			final int x,
			final int y,
			final int z )
	{
		putBit( x | y << 4 | z << 8, 0 );
	}

	private int fixShorts(
			final short s )
	{
		return s & 0xffff;
	}

	@Override
	public int getSafe(
			final int x,
			final int y,
			final int z )
	{
		if(x >= 0 && x < DIMENSION && y >= 0 && y < DIMENSION && z >= 0 && z < DIMENSION)
		{
			return get( x, y, z );
		}

		return 0;
	}

	public static class VisibleFace
	{
		public boolean isEdge;
		public boolean visibleFace;
		public int state;
	};

	public void updateVisibleFace(
			final Direction face,
			int x,
			int y,
			int z,
			final VisibleFace dest,
			final VoxelBlob secondBlob,
			final ICullTest cullVisTest )
	{
		final int mySpot = get( x, y, z );
		dest.state = mySpot;

		x += face.getXOffset();
		y += face.getYOffset();
		z += face.getZOffset();

		if(x >= 0 && x < DIMENSION && y >= 0 && y < DIMENSION && z >= 0 && z < DIMENSION)
		{
			dest.isEdge = false;
			dest.visibleFace = cullVisTest.isVisible( mySpot, get( x, y, z ) );
		}
		else if ( secondBlob != null )
		{
			dest.isEdge = true;
			dest.visibleFace = cullVisTest.isVisible(mySpot, secondBlob.get(x - face.getXOffset() * DIMENSION, y - face.getYOffset() * DIMENSION, z - face.getZOffset() * DIMENSION));
		}
		else
		{
			dest.isEdge = true;
			dest.visibleFace = mySpot != 0;
		}
	}

	public Map<Integer, Integer> getBlockSums()
	{
		final Map<Integer, Integer> counts = new HashMap<Integer, Integer>();

		int lastType = values[0];
		int firstOfType = 0;

		for(int x = 1; x < ARRAY_SIZE; x++)
		{
			final int v = values[x];

			if ( lastType != v )
			{
				final Integer sumx = counts.get( lastType );

				if ( sumx == null )
				{
					counts.put( lastType, x - firstOfType );
				}
				else
				{
					counts.put( lastType, sumx + ( x - firstOfType ) );
				}

				// new count.
				firstOfType = x;
				lastType = v;
			}
		}

		final Integer sumx = counts.get( lastType );

		if ( sumx == null )
		{
			counts.put(lastType, ARRAY_SIZE - firstOfType);
		}
		else
		{
			counts.put(lastType, sumx + (ARRAY_SIZE - firstOfType));
		}

		return counts;
	}

	public List<StateCount> getStateCounts()
	{
		final Map<Integer, Integer> count = getBlockSums();

		final List<StateCount> out;
		out = new ArrayList<StateCount>( count.size() );

		for ( final Entry<Integer, Integer> o : count.entrySet() )
		{
			out.add( new StateCount( o.getKey(), o.getValue() ) );
		}
		return out;
	}

	/**
	 * Get the state id of the most common blockstate.
	 * Will return 0 if the block is empty.
	 */
	public int getMostCommonStateId() {
		return getBlockSums().entrySet().parallelStream()
				.filter(f -> f.getKey()!=0) //We ignore air in the calculation.
				.max(Comparator.comparing(Entry::getValue)).map(Entry::getKey)
				.orElse(0); //There needs to be handling downstream if this happens. This also means the block is empty.
	}

	public VoxelBlob offset(
			final int xx,
			final int yy,
			final int zz )
	{
		final VoxelBlob out = new VoxelBlob();

		for(int z = 0; z < DIMENSION; z++) {
			for(int y = 0; y < DIMENSION; y++)
			{
				for(int x = 0; x < DIMENSION; x++)
				{
					out.set( x, y, z, getSafe( x - xx, y - yy, z - zz ) );
				}
			}
		}

		return out;
	}

	@OnlyIn( Dist.CLIENT )
	public List<String> listContents(
			final List<String> details )
	{
		final HashMap<Integer, Integer> states = new HashMap<Integer, Integer>();
		final HashMap<String, Integer> contents = new HashMap<String, Integer>();

		final BitIterator bi = new BitIterator();
		while ( bi.hasNext() )
		{
			final int state = bi.getNext( this );
			if ( state == 0 )
			{
				continue;
			}

			Integer count = states.get( state );

			if ( count == null )
			{
				count = 1;
			}
			else
			{
				count++;
			}

			states.put( state, count );
		}

		for ( final Entry<Integer, Integer> e : states.entrySet() )
		{
			final String name = null; //TODO ItemChiseledBit.getBitTypeName( ItemChiseledBit.createStack( e.getKey(), 1, false ) );

			if ( name == null )
			{
				continue;
			}

			Integer count = contents.get( name );

			if ( count == null )
			{
				count = e.getValue();
			}
			else
			{
				count += e.getValue();
			}

			contents.put( name, count );
		}

		if ( contents.isEmpty() )
		{
			details.add("Empty");
		}

		for ( final Entry<String, Integer> e : contents.entrySet() )
		{
			details.add( new StringBuilder().append( e.getValue() ).append( ' ' ).append( e.getKey() ).toString() );
		}

		return details;
	}

	public int getSideFlags(
			final int minRange,
			final int maxRange,
			final int totalRequired )
	{
		int output = 0x00;

		for ( final Direction face : Direction.values() )
		{
			final int edge = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 15 : 0;
			int required = totalRequired;

			switch ( face.getAxis() )
			{
				case X:
					for ( int z = minRange; z <= maxRange; z++ )
					{
						for ( int y = minRange; y <= maxRange; y++ )
						{
							if ( getVoxelType( edge, y, z ) == VoxelType.SOLID )
							{
								required--;
							}
						}
					}
					break;
				case Y:
					for ( int z = minRange; z <= maxRange; z++ )
					{
						for ( int x = minRange; x <= maxRange; x++ )
						{
							if ( getVoxelType( x, edge, z ) == VoxelType.SOLID )
							{
								required--;
							}
						}
					}
					break;
				case Z:
					for ( int y = minRange; y <= maxRange; y++ )
					{
						for ( int x = minRange; x <= maxRange; x++ )
						{
							if ( getVoxelType( x, y, edge ) == VoxelType.SOLID )
							{
								required--;
							}
						}
					}
					break;
				default:
					throw new NullPointerException();
			}

			if ( required <= 0 )
			{
				output |= 1 << face.ordinal();
			}
		}

		return output;
	}

	/*public static boolean isFluid(
			final int ref )
	{
		return fluidFilterState.get( ref & 0xffff );
	}

	public boolean filterFluids(
			final boolean wantsFluids )
	{
		boolean hasValues = false;

		for ( int x = 0; x < ARRAY_SIZE; x++ )
		{
			final int ref = values[x];
			if ( ref == 0 )
			{
				continue;
			}

			if ( fluidFilterState.get( ref & 0xffff ) != wantsFluids )
			{
				values[x] = 0;
			}
			else
			{
				hasValues = true;
			}
		}

		return hasValues;
	}*/

	/*public boolean filter(
			final BlockRenderLayer layer )
	{
		final BitSet layerFilterState = layerFilters.get( layer );
		boolean hasValues = false;

		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			final int ref = values[x];
			if ( ref == 0 )
			{
				continue;
			}

			if ( !layerFilterState.get( ref ) )
			{
				values[x] = 0;
			}
			else
			{
				hasValues = true;
			}
		}

		return hasValues;
	}*/

	public void blobFromBytes(
			final byte[] bytes ) throws IOException
	{
		final ByteArrayInputStream out = new ByteArrayInputStream( bytes );
		read( out );
	}

	private void read(
			final ByteArrayInputStream o ) throws IOException, RuntimeException
	{
		final InflaterInputStream w = new InflaterInputStream( o );
		final ByteBuffer bb = BlobSerilizationCache.getCacheBuffer();

		int usedBytes = 0;
		int rv = 0;

		do
		{
			usedBytes += rv;
			rv = w.read( bb.array(), usedBytes, bb.limit() - usedBytes );
		}
		while ( rv > 0 );

		final PacketBuffer header = new PacketBuffer( Unpooled.wrappedBuffer( bb ) );

		final int version = header.readVarInt();
		VoxelVersions versions = VoxelVersions.getVersion(version);
		if(versions==VoxelVersions.ANY) throw new RuntimeException( "Invalid Version: " + version );

		try {
			VoxelSerializer bs = versions.getWorker();
			bs.inflate(header);

			final int byteOffset = header.readVarInt();
			final int bytesOfInterest = header.readVarInt();

			final BitStream bits = BitStream.valueOf( byteOffset, ByteBuffer.wrap( bb.array(), header.readerIndex(), bytesOfInterest ) );
			for(int x = 0; x < ARRAY_SIZE; x++)
			{
				values[x] = bs.readVoxelStateID( bits );// src.get();
			}
		} catch(Exception x) { x.printStackTrace(); }

		w.close();
	}

	private static int bestBufferSize = 26;
	public byte[] blobToBytes(final int version) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream(bestBufferSize);
		write( out, getSerializer(version) );
		final byte[] o = out.toByteArray();

		if(bestBufferSize < o.length) bestBufferSize = o.length;
		return o;
	}

	@Nullable
	private VoxelSerializer getSerializer(
			final int version )
	{
		VoxelVersions ret = VoxelVersions.getVersion(version);
		if(ret==VoxelVersions.ANY) throw new RuntimeException( "Invalid Version: " + version );
		try {
			VoxelSerializer worker = ret.getWorker();
			worker.deflate(this);
			return worker;
		} catch(Exception x) { x.printStackTrace(); }
		return null;
	}

	private void write(
			final ByteArrayOutputStream o,
			final VoxelSerializer bs )
	{
		try
		{
			final Deflater def = BlobSerilizationCache.getCacheDeflater();
			final DeflaterOutputStream w = new DeflaterOutputStream( o, def, bestBufferSize );

			final PacketBuffer pb = BlobSerilizationCache.getCachePacketBuffer();
			pb.writeVarInt( bs.getVersion().getId() );
			bs.write( pb );

			final BitStream set = BlobSerilizationCache.getCacheBitStream();
			for(int x = 0; x < ARRAY_SIZE; x++)
			{
				bs.writeVoxelState( values[x], set );
			}

			final byte[] arrayContents = set.toByteArray();
			final int bytesToWrite = arrayContents.length;
			final int byteOffset = set.byteOffset();

			pb.writeVarInt( byteOffset );
			pb.writeVarInt( bytesToWrite - byteOffset );

			w.write( pb.array(), 0, pb.writerIndex() );

			w.write( arrayContents, byteOffset, bytesToWrite - byteOffset );

			w.finish();
			w.close();

			def.reset();

			o.close();
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	//--- STATIC CONSTRUCTION METHODS ---

	/**
	 * Creates a voxelblob filled with type.
	 */
	public static VoxelBlob full(final BlockState type) {
		return new VoxelBlob().fill(ModUtil.getStateId(type));
	}

	//--- OBJECT OVERWRITE METHODS ---

	@Override
	public VoxelBlob clone() {
		return new VoxelBlob(this);
	}

	@Override
	public boolean equals(final Object obj) {
		if(obj instanceof VoxelBlob) {
			final VoxelBlob a = (VoxelBlob) obj;
			return Arrays.equals(a.values, values);
		}
		return false;
	}
}
