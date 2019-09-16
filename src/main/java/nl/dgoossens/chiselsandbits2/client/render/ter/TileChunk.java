package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

import java.util.Iterator;
import java.util.List;

public class TileChunk extends RenderCache {
    private final TileList tiles = new TileList();

    /**
     * Create the chunk by scanning all tile entities and registering them without triggering
     * rebuild. This saves us from having to do multiple rebuilds when the world loads in.
     */
    public TileChunk(ChiseledBlockTileEntity tileEntity) {
        int chunkPosX = tileEntity.getPos().getX();
        int chunkPosY = tileEntity.getPos().getY();
        int chunkPosZ = tileEntity.getPos().getZ();

        final int mask = ~0xf;
        chunkPosX = chunkPosX & mask;
        chunkPosY = chunkPosY & mask;
        chunkPosZ = chunkPosZ & mask;

        for(int x = 0; x < 16; ++x) {
            for(int y = 0; y < 16; ++y) {
                for(int z = 0; z < 16; ++z) {
                    final TileEntity te = tileEntity.getWorld().getTileEntity(new BlockPos( chunkPosX + x, chunkPosY + y, chunkPosZ + z));
                    if(te instanceof ChiseledBlockTileEntity)
                        register((ChiseledBlockTileEntity) te, false);
                }
            }
        }
    }

    public void register(final ChiseledBlockTileEntity which, final boolean countRebuild) {
        if(which == null) throw new NullPointerException();

        tiles.getWriteLock().lock();
        try {
            if(!tiles.contains(which) && countRebuild) rebuild();
            tiles.add(which);
        } finally {
            tiles.getWriteLock().unlock();
        }
    }

    public void unregister(final ChiseledBlockTileEntity which, final boolean countRebuild) {
        tiles.getWriteLock().lock();

        try {
            if(tiles.contains(which) && countRebuild) rebuild();
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
