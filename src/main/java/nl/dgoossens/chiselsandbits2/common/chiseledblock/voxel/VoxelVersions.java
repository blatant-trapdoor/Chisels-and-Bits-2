package nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel;

import nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization.BlobSerializer;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization.VoxelSerializer;

import java.util.stream.Stream;

public enum VoxelVersions {
    ANY(-1),
    COMPACT(1, BlobSerializer.class),
    ;

    private int id;
    private Class<? extends VoxelSerializer> worker;
    VoxelVersions(int id) { this(id, null); }
    VoxelVersions(int id, Class<? extends VoxelSerializer> klass) {
        this.id=id; this.worker=klass;
    }

    /**
     * Get the current default voxel verison.
     */
    public static int getDefault() { return COMPACT.getId(); }
    /**
     * Get this versions id.
     */
    public int getId() { return id; }
    /**
     * Get the object that does all the work, the voxel serializer.
     */
    public VoxelSerializer getWorker() throws Exception { return worker!=null ? worker.newInstance() : null; }

    /**
     * Get the voxel version using the id specified.
     */
    public static VoxelVersions getVersion(int i) {
        return Stream.of(VoxelVersions.values()).filter(f -> f.id==i).findAny().orElse(ANY);
    }
}
