package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import io.netty.buffer.Unpooled;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import nl.dgoossens.chiselsandbits2.api.ICullTest;
import nl.dgoossens.chiselsandbits2.api.IVoxelSrc;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization.BitStream;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization.BlobSerilizationCache;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization.VoxelSerializer;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class VoxelBlob implements IVoxelSrc {
    public final static int AIR_BIT = 0;

    public final static int DIMENSION = 16;
    public final static int DIMENSION2 = DIMENSION * DIMENSION;
    public final static int DIMENSION3 = DIMENSION2 * DIMENSION;

    public final static int ARRAY_SIZE = DIMENSION3;
    public final static int DIMENSION_MINUS_ONE = DIMENSION - 1;

    public final static VoxelBlob NULL_BLOB = new VoxelBlob();

    private final int[] values = new int[ARRAY_SIZE];
    private int best_buffer_size = 26;
    //Every int in the values map is used as follows:
    //  00000000000000000000000000000000

    // The integer's 2 MSB determine the type:
    // 00 -> legacy
    // 01 -> fluidstate
    // 10 -> colouredstate
    // 11 -> blockstate

    // Each type has it's own usage of the remaining 30 bits
    // Coloured State -> 30: 6 - 8 - 8 - 8
    // which leaves 64 possible alpha's and 255 for R, G and B
    // the alpha value is retrieved by multiplying the value from the 6 bits by 4

    //--- CONSTRUCTORS ---
    public VoxelBlob() {
    }

    protected VoxelBlob(final VoxelBlob vb) {
        for (int x = 0; x < values.length; ++x)
            values[x] = vb.values[x];
    }

    /**
     * Creates a voxelblob filled with type.
     */
    public static VoxelBlob full(final BlockState type) {
        return new VoxelBlob().fill(ModUtil.getStateId(type));
    }

    //--- PROTECTED METHODS ---
    protected int getBit(final int offset) {
        return values[offset];
    }

    //--- MODIFICATION METHODS ---

    protected void putBit(final int offset, final int newValue) {
        values[offset] = newValue;
    }

    /**
     * Merges the second voxelblob into this one.
     * Returns this.
     */
    public VoxelBlob merge(final VoxelBlob second) {
        for (int x = 0; x < values.length; ++x)
            if (values[x] == 0) values[x] = second.values[x];
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
        while (bi.hasNext()) {
            if (bi.getNext(this) != AIR_BIT) {
                switch (axis) {
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

    /**
     * Offsets the voxel blob.
     */
    public VoxelBlob offset(final int xx, final int yy, final int zz) {
        final VoxelBlob out = new VoxelBlob();
        for (int z = 0; z < DIMENSION; z++) {
            for (int y = 0; y < DIMENSION; y++) {
                for (int x = 0; x < DIMENSION; x++)
                    out.set(x, y, z, getSafe(x - xx, y - yy, z - zz));
            }
        }
        return out;
    }

    /**
     * Spin the voxel blob along the axis by 90
     * clockwise.
     */
    public VoxelBlob spin(final Direction.Axis axis) {
        final VoxelBlob out = new VoxelBlob();
        //Rotate by -90 Degrees: x' = y y' = - x

        final BitIterator bi = new BitIterator();
        while (bi.hasNext()) {
            switch (axis) {
                case X:
                    out.set(bi.x, DIMENSION_MINUS_ONE - bi.z, bi.y, bi.getNext(this));
                    break;
                case Y:
                    out.set(bi.z, bi.y, DIMENSION_MINUS_ONE - bi.x, bi.getNext(this));
                    break;
                case Z:
                    out.set(DIMENSION_MINUS_ONE - bi.y, bi.x, bi.z, bi.getNext(this));
                    break;
                default:
                    throw new NullPointerException();
            }
        }
        return out;
    }

    /**
     * Fills this voxelbob
     */
    public VoxelBlob fill(final int value) {
        for (int x = 0; x < ARRAY_SIZE; x++)
            values[x] = value;
        return this;
    }

    //--- LOOKUP METHODS ---

    /**
     * Clears this voxelblob, fills it with air.
     */
    public VoxelBlob clear() {
        fill(AIR_BIT);
        return this;
    }

    /**
     * Will return true if for every bit in the voxelblob
     * it is true that it is air in either or both of
     * the blobs. (no bit is set in both)
     */
    public boolean canMerge(final VoxelBlob second) {
        for (int x = 0; x < values.length; ++x)
            if (values[x] != AIR_BIT && second.values[x] != AIR_BIT) return false;

        return true;
    }

    /**
     * Get the position of the center of the shape.
     */
    public BlockPos getCenter() {
        final IntegerBox bounds = getBounds();
        return bounds != null ? new BlockPos((bounds.minX + bounds.maxX) / 2, (bounds.minY + bounds.maxY) / 2, (bounds.minZ + bounds.maxZ) / 2) : null;
    }

    /**
     * Gets the bounding box around this voxel blob.
     */
    public IntegerBox getBounds() {
        boolean found = false;
        int min_x = 15, min_y = 15, min_z = 15;
        int max_x = 0, max_y = 0, max_z = 0;

        final BitIterator bi = new BitIterator();
        while (bi.hasNext()) {
            if (bi.getNext(this) != AIR_BIT) {
                found = true;
                min_x = Math.min(min_x, bi.x);
                min_y = Math.min(min_y, bi.y);
                min_z = Math.min(min_z, bi.z);

                max_x = Math.max(max_x, bi.x);
                max_y = Math.max(max_y, bi.y);
                max_z = Math.max(max_z, bi.z);
            }
        }
        return found ? new IntegerBox(min_x, min_y, min_z, max_x, max_y, max_z) : IntegerBox.NULL;
    }

    /**
     * Returns the amount of bits that's equal to air in this blob.
     */
    public long air() {
        int i = 0;
        for(int v : values)
            if(v == AIR_BIT) i++;
        return i;
    }

    /**
     * Returns the amount of fluid bits in the block.
     */
    public long fluids() {
        int i = 0;
        for(int v : values)
            if(VoxelType.isFluid(v)) i++;
        return i;
    }

    /**
     * Returns the amount of bits that's not air.
     */
    public long filled() {
        int i = 0;
        for(int v : values)
            if(v != VoxelBlob.AIR_BIT) i++;
        return i;
    }

    /**
     * Returns {@link #AIR_BIT} if this blob is made up of several
     * bit types. Otherwise returns the bit type this is made of.
     */
    public int singleType() {
        int i = values[0];
        for(int v : values) {
            //If that value is not the same we return AIR_BIT.
            if(v != i)
                return AIR_BIT;
        }
        return i;
    }

    /**
     * Get the state id of the most common state.
     * Will return 0 if the block is empty.
     */
    public int getMostCommonStateId() {
        return getBlockSums().entrySet().parallelStream()
                .filter(f -> f.getKey() != AIR_BIT) //We ignore air in the calculation.
                .max(Comparator.comparing(e -> e.getValue().intValue())).map(Entry::getKey)
                .orElse(AIR_BIT); //There needs to be handling downstream if this happens. This also means the block is empty.
    }

    /**
     * Get the state id of the most common fluid.
     * Will return 0 if the block is empty.
     * Used to determine which fluidstate the block should use.
     */
    public int getMostCommonFluid() {
        return getBlockSums().entrySet().parallelStream()
                .filter(f -> VoxelType.isFluid(f.getKey())) //We ignore non-fluids in the calculation.
                .max(Comparator.comparing(e -> e.getValue().intValue())).map(Entry::getKey)
                .orElse(AIR_BIT); //There needs to be handling downstream if this happens. This also means the block is empty.
    }

    /**
     * Returns a set of all bit ids in this voxel blob.
     */
    public Set<Integer> listContents() {
        return Arrays.stream(values).parallel().distinct().boxed().collect(Collectors.toSet());
    }

    //--- ACTION METHODS ---

    /**
     * Returns a map with bit-count sums of all bits in this
     * voxelblob.
     * LongAdder is used to allow for multithreaded counting.
     */
    public Map<Integer, LongAdder> getBlockSums() {
        final Map<Integer, LongAdder> counts = new ConcurrentHashMap<>();
        Arrays.stream(values).parallel()
                .forEach(f -> {
                    if (!counts.containsKey(f)) counts.put(f, new LongAdder());
                    counts.get(f).increment();
                });
        return counts;
    }

    /**
     * Get the voxel type of a bit at a position.
     */
    public VoxelType getVoxelType(final int x, final int y, final int z) {
        return VoxelType.getType(get(x, y, z));
    }

    /**
     * Gets a pit at a given position.
     */
    public int get(final int x, final int y, final int z) {
        return getBit(x | y << 4 | z << 8);
    }

    /**
     * Sets a bit at a given x/y/z to a value.
     */
    public void set(final int x, final int y, final int z, final int value) {
        putBit(x | y << 4 | z << 8, value);
    }

    /**
     * Sets a bit to air a given position.
     */
    public void clear(final int x, final int y, final int z) {
        putBit(x | y << 4 | z << 8, AIR_BIT);
    }

    //--- STATIC CONSTRUCTION METHODS ---

    /**
     * Get the bit at a given location. Doesn't throw errors when
     * the coordinates are not in this voxel blob.
     */
    @Override
    public int getSafe(final int x, final int y, final int z) {
        if (x >= 0 && x < DIMENSION && y >= 0 && y < DIMENSION && z >= 0 && z < DIMENSION)
            return get(x, y, z);
        return AIR_BIT;
    }

    //--- OBJECT OVERWRITE METHODS ---
    @Override
    public VoxelBlob clone() {
        return new VoxelBlob(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof VoxelBlob) {
            final VoxelBlob a = (VoxelBlob) obj;
            return Arrays.equals(a.values, values);
        }
        return false;
    }

    //--- INTERNAL LOGIC METHODS ---

    /**
     * Updates the visible faces.
     */
    public void updateFaceVisibility(final Direction face, int x, int y, int z, final VisibleFace dest, final VoxelBlob secondBlob, final ICullTest cullVisTest) {
        final int mySpot = get(x, y, z);
        dest.state = mySpot;

        x += face.getXOffset();
        y += face.getYOffset();
        z += face.getZOffset();

        if (x >= 0 && x < DIMENSION && y >= 0 && y < DIMENSION && z >= 0 && z < DIMENSION) {
            dest.isEdge = false;
            dest.visibleFace = cullVisTest.isVisible(mySpot, get(x, y, z));
        } else {
            dest.isEdge = true;
            dest.visibleFace = (secondBlob == null ? (mySpot != AIR_BIT) :
                    (cullVisTest.isVisible(mySpot, secondBlob.get(x - face.getXOffset() * DIMENSION, y - face.getYOffset() * DIMENSION, z - face.getZOffset() * DIMENSION))));
        }
    }

    /**
     * Special internal method for getting sideflags.
     */
    public int getSideFlags(final int minRange, final int maxRange, final int totalRequired) {
        int output = 0x00;

        for (final Direction face : Direction.values()) {
            final int edge = face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 15 : 0;
            int required = totalRequired;

            switch (face.getAxis()) {
                case X:
                    for (int z = minRange; z <= maxRange; z++) {
                        for (int y = minRange; y <= maxRange; y++) {
                            if (getVoxelType(edge, y, z).isSolid()) required--;
                        }
                    }
                    break;
                case Y:
                    for (int z = minRange; z <= maxRange; z++) {
                        for (int x = minRange; x <= maxRange; x++) {
                            if (getVoxelType(x, edge, z).isSolid()) required--;
                        }
                    }
                    break;
                case Z:
                    for (int y = minRange; y <= maxRange; y++) {
                        for (int x = minRange; x <= maxRange; x++) {
                            if (getVoxelType(x, y, edge).isSolid()) required--;
                        }
                    }
                    break;
            }

            if (required <= 0) output |= 1 << face.ordinal();
        }
        return output;
    }

	/*@OnlyIn(Dist.CLIENT)
	public List<String> listContents(final List<String> details) {
		final HashMap<Integer, Integer> states = new HashMap<>();
		final HashMap<String, Integer> contents = new HashMap<>();

		final BitIterator bi = new BitIterator();
		while ( bi.hasNext() )
		{
			final int state = bi.getNext( this );
			if ( state == 0 )
			{
				continue;
			}

			Integer count = states.fromName( state );

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

			Integer count = contents.fromName( name );

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
	}*/

    /**
     * Builds a blob from a byte array.
     */
    public void blobFromBytes(final byte[] bytes) throws IOException {
        try {
            if (bytes.length < 1) {
                throw new RuntimeException("Unable to load VoxelBlob: length of data was 0");
            }
            final ByteArrayInputStream out = new ByteArrayInputStream(bytes);
            read(out);
        } catch (Exception x) {
            throw new RuntimeException("Unable to load VoxelBlob", x);
        }
    }

    //--- FILTERING ---
	/*
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

			if ( fluidFilterState.fromName( ref & 0xffff ) != wantsFluids )
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
    //TODO add a new filter method which returns only one voxel type
	/*public boolean filter(
			final BlockRenderLayer layer )
	{
		final BitSet layerFilterState = layerFilters.fromName( layer );
		boolean hasValues = false;

		for(int x = 0; x < ARRAY_SIZE; x++)
		{
			final int ref = values[x];
			if ( ref == 0 )
			{
				continue;
			}

			if ( !layerFilterState.fromName( ref ) )
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

    //--- SERIALIZATION ---

    /**
     * Reads this voxelblobs values from the supplied ByteArrayInputStream.
     */
    private void read(final ByteArrayInputStream o) throws IOException, RuntimeException {
        final InflaterInputStream w = new InflaterInputStream(o);
        final ByteBuffer bb = BlobSerilizationCache.getCacheBuffer();

        int usedBytes = 0;
        int rv = 0;

        do {
            usedBytes += rv;
            rv = w.read(bb.array(), usedBytes, bb.limit() - usedBytes);
        } while (rv > 0);
        w.close();

        final PacketBuffer header = new PacketBuffer(Unpooled.wrappedBuffer(bb));
        final int version = header.readVarInt();
        VoxelVersions versions = VoxelVersions.getVersion(version);
        if (versions == VoxelVersions.ANY) throw new RuntimeException("Invalid Version: " + version);

        try {
            VoxelSerializer bs = versions.getWorker();
            if (bs == null) throw new RuntimeException("Invalid VoxelVersion: " + version + ", worker was null");
            bs.inflate(header);

            final int byteOffset = header.readVarInt();
            final int bytesOfInterest = header.readVarInt();

            final BitStream bits = BitStream.valueOf(byteOffset, ByteBuffer.wrap(bb.array(), header.readerIndex(), bytesOfInterest));
            for (int x = 0; x < ARRAY_SIZE; x++)
                values[x] = bs.readVoxelStateID(bits);
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Creates a byte array representing this blob.
     */
    public byte[] blobToBytes(final int version) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(best_buffer_size);
        write(out, getSerializer(version));
        final byte[] o = out.toByteArray();
        if (best_buffer_size < o.length) best_buffer_size = o.length;
        return o;
    }

    /**
     * Get the serializer to use to serialize this
     * blob with the specified version.
     */
    @Nullable
    private VoxelSerializer getSerializer(final int version) {
        VoxelVersions ret = VoxelVersions.getVersion(version);
        if (ret == VoxelVersions.ANY) throw new RuntimeException("Invalid Version: " + version);
        try {
            VoxelSerializer worker = ret.getWorker();
            if (worker == null) return null;
            worker.deflate(this);
            return worker;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }

    /**
     * Write this blob to a ByteArrayOutputStream.
     */
    private void write(final ByteArrayOutputStream o, @Nullable final VoxelSerializer bs) {
        if (bs == null) return;
        try {
            final Deflater def = BlobSerilizationCache.getCacheDeflater();
            final DeflaterOutputStream w = new DeflaterOutputStream(o, def, best_buffer_size);

            final PacketBuffer pb = BlobSerilizationCache.getCachePacketBuffer();
            pb.writeVarInt(bs.getVersion().getId());
            bs.write(pb);

            final BitStream set = BlobSerilizationCache.getCacheBitStream();
            for (int x = 0; x < ARRAY_SIZE; x++)
                bs.writeVoxelState(values[x], set);

            final byte[] arrayContents = set.toByteArray();
            final int bytesToWrite = arrayContents.length;
            final int byteOffset = set.byteOffset();

            pb.writeVarInt(byteOffset);
            pb.writeVarInt(bytesToWrite - byteOffset);

            w.write(pb.array(), 0, pb.writerIndex());
            w.write(arrayContents, byteOffset, bytesToWrite - byteOffset);

            w.finish();
            w.close();
            o.close();
            def.reset();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class VisibleFace {
        public boolean isEdge;
        public boolean visibleFace;
        public int state;
    }
}
