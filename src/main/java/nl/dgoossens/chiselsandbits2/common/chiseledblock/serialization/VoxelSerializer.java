package nl.dgoossens.chiselsandbits2.common.chiseledblock.serialization;

import net.minecraft.network.PacketBuffer;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;

public interface VoxelSerializer {
    /**
     * Get the associated voxel version.
     */
    VoxelVersions getVersion();

    void write(final PacketBuffer buffer);

    /**
     * Read a bit id from the stream.
     */
    void writeVoxelState(final int stateId, final BitStream stream);

    /**
     * Write a bit id to the stream.
     */
    int readVoxelStateID(final BitStream stream);

    void deflate(final VoxelBlob toDeflate);

    void inflate(final PacketBuffer toInflate);
}
