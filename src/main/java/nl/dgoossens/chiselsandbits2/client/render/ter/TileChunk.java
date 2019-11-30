package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class TileChunk extends RenderCache {
    //Every tile chunk is one quarter chunk.
    public static final int TILE_CHUNK_SIZE = 8;
    public static final int CHUNK_COORDINATE_MASK = 0xfffffff8;

    private final Collection<ChiseledBlockTileEntity> tiles = new HashSet<>();
    private long lastValidation = 0;

    /**
     * Create the chunk by scanning all tile entities and registering them without triggering
     * rebuild. This saves us from having to do multiple rebuilds when the world loads in.
     */
    public TileChunk(ChiseledBlockTileEntity tileEntity) {
        int chunkPosX = tileEntity.getPos().getX() & CHUNK_COORDINATE_MASK;
        int chunkPosY = tileEntity.getPos().getY() & CHUNK_COORDINATE_MASK;
        int chunkPosZ = tileEntity.getPos().getZ() & CHUNK_COORDINATE_MASK;

        for (int x = 0; x < TILE_CHUNK_SIZE; ++x) {
            for (int y = 0; y < TILE_CHUNK_SIZE; ++y) {
                for (int z = 0; z < TILE_CHUNK_SIZE; ++z) {
                    final TileEntity te = tileEntity.getWorld().getTileEntity(new BlockPos(chunkPosX + x, chunkPosY + y, chunkPosZ + z));
                    if (te instanceof ChiseledBlockTileEntity)
                        register((ChiseledBlockTileEntity) te, false);
                }
            }
        }
    }

    public void register(final ChiseledBlockTileEntity which, final boolean countRebuild) {
        if (which == null) throw new NullPointerException();

        if (!tiles.contains(which) && countRebuild) rebuild();
        tiles.add(which);
    }

    public void unregister(final ChiseledBlockTileEntity which, final boolean countRebuild) {
        if (tiles.contains(which) && countRebuild) rebuild();
        tiles.remove(which);
    }

    /**
     * Inform the tile chunk that this tile specifically has been
     * updated and all neighbours need changing.
     */
    public void update(final ChiseledBlockTileEntity which) {
        which.getRenderTracker().invalidate();
        rebuild();
    }

    public void validate() {
        //Don't validate more often than every 5ms.
        if(System.currentTimeMillis() - lastValidation > 5) {
            lastValidation = System.currentTimeMillis();
            boolean invalid = false;

            //Validate the neighbours for all TE's in the chunk
            for (final ChiseledBlockTileEntity te : tiles)
                invalid = invalid || te.getRenderTracker().validate(te.getWorld(), te.getPos());

            //Make sure to only rebuild once
            if(invalid)
                rebuild();
        }
    }

    /**
     * Return the coordinates of the (x, y, z) of the bottom
     * corner (0, 0, 0) of this chunk.
     */
    public BlockPos chunkOffset() {
        if (getTileList().isEmpty()) return BlockPos.ZERO;

        final BlockPos tilepos = getTileList().iterator().next().getPos();
        return new BlockPos(tilepos.getX() & CHUNK_COORDINATE_MASK, tilepos.getY() & CHUNK_COORDINATE_MASK, tilepos.getZ() & CHUNK_COORDINATE_MASK);
    }

    /**
     * Get the list of tile entities in this chunk.
     */
    public Collection<ChiseledBlockTileEntity> getTileList() {
        return tiles;
    }
}
