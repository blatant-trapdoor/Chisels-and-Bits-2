package nl.dgoossens.chiselsandbits2.common.chiseledblock;

import nl.dgoossens.chiselsandbits2.api.render.IStateRef;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;

public class BlockStateRef implements IStateRef {
    final int stateID;

    public BlockStateRef(
            final int sid) {
        stateID = sid;
    }

    @Override
    public boolean equals(
            final Object obj) {
        if (obj instanceof BlockStateRef) {
            return stateID == ((BlockStateRef) obj).stateID;
        }

        return false;
    }

    @Override
    public VoxelBlob getVoxelBlob() {
        final VoxelBlob b = new VoxelBlob();
        b.fill(stateID);
        return b;
    }

};