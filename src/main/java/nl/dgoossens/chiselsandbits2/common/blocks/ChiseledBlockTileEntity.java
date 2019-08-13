package nl.dgoossens.chiselsandbits2.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.modes.BoxType;
import nl.dgoossens.chiselsandbits2.client.render.ter.RenderCache;
import nl.dgoossens.chiselsandbits2.client.render.ter.TileChunk;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelNeighborRenderTracker;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nonnull;

public class ChiseledBlockTileEntity extends TileEntity {
    public ChiseledBlockTileEntity() { super(ChiselsAndBits2.getBlocks().CHISELED_BLOCK_TILE); }
    private TileChunk chunk; //The rendering chunk this block belongs to.
    private RenderCache renderCache = new RenderCache(); //The cached data for this blocks' rendering.

    public static final ModelProperty<VoxelBlobStateReference> VOXEL_DATA = new ModelProperty<>();
    public static final ModelProperty<Integer> PRIMARY_BLOCKSTATE = new ModelProperty<>();
    public static final ModelProperty<VoxelNeighborRenderTracker> NEIGHBOUR_RENDER_TRACKER = new ModelProperty<>();

    private VoxelShape cachedShape, collisionShape;
    private int primaryBlock;
    private VoxelBlobStateReference voxelBlob;
    private VoxelNeighborRenderTracker renderTracker;

    public int getPrimaryBlock() { return primaryBlock; }
    public void setPrimaryBlock(int d) {
        primaryBlock=d;
        requestModelDataUpdate();
    }
    public VoxelBlobStateReference getVoxelReference() { return voxelBlob; }
    private void setVoxelReference(VoxelBlobStateReference voxel) {
        voxelBlob=voxel;
        requestModelDataUpdate();
        recalculateShape();
        renderCache.rebuild();
    }
    public VoxelNeighborRenderTracker getRenderTracker() {
        if(renderTracker==null) renderTracker = new VoxelNeighborRenderTracker();
        return renderTracker;
    }
    public void setRenderTracker(VoxelNeighborRenderTracker d) {
        renderTracker=d;
        requestModelDataUpdate();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        IModelData imd = super.getModelData();
        imd.setData(VOXEL_DATA, getVoxelReference());
        imd.setData(PRIMARY_BLOCKSTATE, getPrimaryBlock());
        imd.setData(NEIGHBOUR_RENDER_TRACKER, getRenderTracker());
        return imd;
    }

    /**
     * Get the cached shape of this block.
     */
    public VoxelShape getCachedShape() {
        if(cachedShape==null) recalculateShape();
        return cachedShape;
    }
    /**
     * Get the cached shape of this block.
     */
    public VoxelShape getCollisionShape() {
        if(collisionShape==null) recalculateShape();
        return collisionShape;
    }

    /**
     * Recalculates the voxel shape.
     */
    public void recalculateShape() {
        VoxelShape base = VoxelShapes.empty();
        if(getVoxelReference()!=null)
            for(AxisAlignedBB box : getVoxelReference().getBoxes(BoxType.SOLID))
                base = VoxelShapes.combine(base, VoxelShapes.create(box), IBooleanFunction.OR);
        collisionShape = base.simplify();

        double minX = 0.5, minY = 0.5, minZ = 0.5, maxX = 0.5, maxY = 0.5, maxZ = 0.5;
        if(getVoxelReference()!=null)
            for(AxisAlignedBB box : getVoxelReference().getBoxes(BoxType.BOTH)) {
                minX = Math.min(box.minX, minX);
                minY = Math.min(box.minY, minY);
                minZ = Math.min(box.minZ, minZ);
                maxX = Math.max(box.maxX, maxX);
                maxY = Math.max(box.maxY, maxY);
                maxZ = Math.max(box.maxZ, maxZ);
            }
        cachedShape = VoxelShapes.create(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * Get the tile chunk this block belongs to.
     */
    public TileChunk getChunk(final IBlockReader world) {
        if(chunk==null) {
            chunk = findRenderChunk(world);
            chunk.register(this); //Register us to be a part of the chunk if this is the first time we're searching.
        }
        return chunk;
    }

    /**
     * Find the rendering chunk this TE belongs to.
     */
    private TileChunk findRenderChunk(final IBlockReader access) {
        int chunkPosX = getPos().getX();
        int chunkPosY = getPos().getY();
        int chunkPosZ = getPos().getZ();

        final int mask = ~0xf;
        chunkPosX = chunkPosX & mask;
        chunkPosY = chunkPosY & mask;
        chunkPosZ = chunkPosZ & mask;

        for ( int x = 0; x < 16; ++x ) {
            for ( int y = 0; y < 16; ++y ) {
                for ( int z = 0; z < 16; ++z ) {
                    final TileEntity te = access.getTileEntity(new BlockPos( chunkPosX + x, chunkPosY + y, chunkPosZ + z));
                    if ( te instanceof ChiseledBlockTileEntity && ((ChiseledBlockTileEntity) te).chunk != null)
                        return ((ChiseledBlockTileEntity) te).chunk;
                }
            }
        }
        return new TileChunk();
    }

    /**
     * Get this tile entity's rendering cache.
     */
    public RenderCache getRenderCache() { return renderCache; }

    @Override
    public boolean hasFastRenderer() { return true; }
    @Override
    public boolean canRenderBreaking() { return true; }

    /**
     * Update this tile entity's voxel data.
     */
    public boolean updateBlob(final NBTBlobConverter converter) {
        final VoxelBlobStateReference originalRef = getVoxelReference();
        final int b = converter.getPrimaryBlockStateID();

        VoxelBlobStateReference voxelRef = null;
        try {
            voxelRef = converter.getVoxelRef(VoxelVersions.getDefault());
        } catch (final Exception e) {
            e.printStackTrace();
            voxelRef = new VoxelBlobStateReference( 0);
        }

        setPrimaryBlock(b);
        setVoxelReference(voxelRef);

        return voxelRef == null || !voxelRef.equals(originalRef);
    }

    /**
     * Set the voxel blob to another.
     */
    public void setBlob(final VoxelBlob vb) {
        int mostCommonState = vb.getMostCommonStateId();

        if(world==null ) {
            if(mostCommonState == 0) {
                if(primaryBlock == 0) mostCommonState = ModUtil.getStateId(Blocks.STONE.getDefaultState());
                else mostCommonState = getPrimaryBlock();
            }
        }

        //TODO properly replace with normal block if this causes block to be full
        /*if ( common.isFullBlock )
        {
            setState( getBasicState()
                    .withProperty( BlockChiseled.UProperty_VoxelBlob, new VoxelBlobStateReference( common.mostCommonState, MathHelper.getPositionRandom( pos ) ) ) );

            final IBlockState newState = ModUtil.getStateById( common.mostCommonState );
            if ( ChiselsAndBits.getConfig().canRevertToBlock( newState ) )
            {
                if ( !MinecraftForge.EVENT_BUS.post( new EventFullBlockRestoration( worldObj, pos, newState ) ) )
                {
                    worldObj.setBlockState( pos, newState, triggerUpdates ? 3 : 0 );
                }
            }
        }*/
        if(mostCommonState == 0) {
            setVoxelReference(new VoxelBlobStateReference(vb.blobToBytes(VoxelVersions.getDefault())));
            setPrimaryBlock(mostCommonState);
            markDirty();
        } else {
            setVoxelReference(new VoxelBlobStateReference( 0));
            world.removeTileEntity(pos);
            world.removeBlock(pos, false);
        }
    }

    public VoxelBlob getBlob()
    {
        VoxelBlob vb = new VoxelBlob();
        final VoxelBlobStateReference vbs = getVoxelReference();

        if(vbs != null) vb = vbs.getVoxelBlob();
        else //If we can't make it proper we should fill it with stone as default.
            vb.fill( ModUtil.getStateId( Blocks.STONE.getDefaultState() ) );

        return vb;
    }

    public void completeEditOperation(
            final VoxelBlob vb )
    {
        final VoxelBlobStateReference before = getVoxelReference();
        setBlob( vb );
        final VoxelBlobStateReference after = getVoxelReference();

        if ( world != null )
            Minecraft.getInstance().worldRenderer.markForRerender(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);

        //TODO UndoTracker.getInstance().add( getWorld(), getPos(), before, after );
    }

    public void fillWith(final BlockState blockType) {
        setVoxelReference(new VoxelBlobStateReference( ModUtil.getStateId( blockType ) ));
    }

    public boolean readChiselData(final CompoundNBT tag) {
        final NBTBlobConverter converter = new NBTBlobConverter( this );
        return converter.readChiselData( tag, VoxelVersions.getDefault() );
    }

    public void writeChiselData(final CompoundNBT tag) {
        new NBTBlobConverter( this ).writeChiselData( tag );
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound) {
        super.write( compound );
        writeChiselData( compound );
        return compound;
    }

    @Override
    public void read(final CompoundNBT compound) {
        super.read( compound );
        readChiselData( compound );
    }
}
