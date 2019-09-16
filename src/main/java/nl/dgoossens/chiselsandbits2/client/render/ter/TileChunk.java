package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.util.math.BlockPos;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

import java.util.Iterator;
import java.util.List;

public class TileChunk {
    private final TileList tiles = new TileList();

    public void register(final ChiseledBlockTileEntity which) {
        if(which == null) throw new NullPointerException();

        tiles.getWriteLock().lock();
        try {
            tiles.add(which);
        } finally {
            tiles.getWriteLock().unlock();
        }
    }


    public void unregister(final ChiseledBlockTileEntity which) {
        tiles.getWriteLock().lock();

        try {
            tiles.remove(which);
        } finally {
            tiles.getWriteLock().unlock();
        }
    }

    /**
     * Return the coordinates of the (x, y, z) of the bottom
     * corner (0, 0, 0) of this chunk.
     */
    public BlockPos chunkOffset() {
        tiles.getReadLock().lock();

        try {
            if(getTiles().isEmpty()) return BlockPos.ZERO;

            final int bitMask = ~0xf; //This bitmask drops the last 4 bits, effectively getting the chunkX.
            final Iterator<ChiseledBlockTileEntity> i = getTiles().iterator();
            final BlockPos tilepos = i.hasNext() ? i.next().getPos() : BlockPos.ZERO;
            return new BlockPos(tilepos.getX() & bitMask, tilepos.getY() & bitMask, tilepos.getZ() & bitMask);
        } finally {
            tiles.getReadLock().unlock();
        }
    }

    /**
     * Get the list of tile entities in this chunk.
     */
    public List<ChiseledBlockTileEntity> getTileList() {
        return tiles.createCopy();
    }

    /**
     * Virtually identical to getTileList();
     */
    public TileList getTiles() {
        return tiles;
    }
}
