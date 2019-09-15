package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;

import net.minecraftforge.fml.loading.FMLEnvironment;
import nl.dgoossens.chiselsandbits2.api.IStateRef;

public final class VoxelBlobStateReference implements IStateRef {
	private static Map<VoxelBlobStateInstance, WeakReference<VoxelBlobStateInstance>> serverRefs = Collections.synchronizedMap( new WeakHashMap<>() );
	private static Map<VoxelBlobStateInstance, WeakReference<VoxelBlobStateInstance>> clientRefs = Collections.synchronizedMap( new WeakHashMap<>() );

	private static byte[] airBlob;

	private static Map<VoxelBlobStateInstance, WeakReference<VoxelBlobStateInstance>> getReferences() {
		return FMLEnvironment.dist.isClient() ? clientRefs : serverRefs;
	}

	private static VoxelBlobStateInstance lookupReference(final VoxelBlobStateInstance inst ) {
		return Optional.ofNullable(getReferences().get(inst)).map(Reference::get).orElse(null);
	}

	private static byte[] findBytesFor(final int stateId ) {
		if (stateId == 0) {
			if ( airBlob == null )
			{
				final VoxelBlob vb = new VoxelBlob();
				airBlob = vb.blobToBytes( VoxelVersions.getDefault() );
			}

			return airBlob;
		}

		final VoxelBlob vb = new VoxelBlob();
		vb.fill(stateId);
		return vb.blobToBytes(VoxelVersions.getDefault());
	}

	private static void addReference(final VoxelBlobStateInstance inst) { getReferences().put( inst, new WeakReference<>( inst ) ); }
	private static VoxelBlobStateInstance findReference(final byte[] v) {
		final VoxelBlobStateInstance t = new VoxelBlobStateInstance( v );
		VoxelBlobStateInstance ref = lookupReference( t );
		if (ref == null) {
			ref = t;
			addReference(t);
		}
		return ref;
	}

	private final VoxelBlobStateInstance data;
	public VoxelBlobStateInstance getInstance() { return data; }
	public byte[] getByteArray() { return data.voxelBytes; }

	@Override
	public VoxelBlob getVoxelBlob() { return data.getBlob(); }
	public VoxelBlob getVoxelBlobCatchable() throws Exception { return data.getBlobCatchable(); }

	public VoxelBlobStateReference() { this(VoxelBlob.AIR_BIT); }
	public VoxelBlobStateReference(final VoxelBlob blob) {
		this(blob.blobToBytes(VoxelVersions.getDefault()));
		data.blob = new SoftReference<>( new VoxelBlob(blob));
	}
	public VoxelBlobStateReference(final int stateId) { this(findBytesFor(stateId)); }
	public VoxelBlobStateReference(final byte[] v) { data = findReference( v ); }

	@Override
	public boolean equals(final Object obj) {
		if(!(obj instanceof VoxelBlobStateReference)) return false;
		return data.equals(((VoxelBlobStateReference) obj).data);
	}
	@Override
	public int hashCode() { return data.hash; }
	public int getFormat() { return data.getFormat(); }
}
