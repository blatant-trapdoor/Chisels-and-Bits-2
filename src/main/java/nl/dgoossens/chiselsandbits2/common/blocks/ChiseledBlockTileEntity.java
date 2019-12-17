package nl.dgoossens.chiselsandbits2.common.blocks;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.bit.VoxelType;
import nl.dgoossens.chiselsandbits2.client.render.ter.TileChunk;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelNeighborRenderTracker;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.BitUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ChiseledBlockTileEntity extends TileEntity {
    public static final ModelProperty<VoxelBlobStateReference> VOXEL_DATA = new ModelProperty<>();
    public static final ModelProperty<VoxelNeighborRenderTracker> NEIGHBOUR_RENDER_TRACKER = new ModelProperty<>();

    private TileChunk chunk; //The rendering chunk this block belongs to.
    private VoxelShape cachedShape, collisionShape;
    private int primaryBlock;
    private VoxelBlobStateReference voxelBlob;
    private VoxelNeighborRenderTracker renderTracker;
    private ItemStack itemCache;

    public ChiseledBlockTileEntity() {
        super(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK_TILE);
    }

    public int getPrimaryBlock() {
        return primaryBlock;
    }

    public void setPrimaryBlock(int d) {
        if (VoxelType.getType(d) == VoxelType.BLOCKSTATE)
            primaryBlock = d;
    }

    public VoxelBlobStateReference getVoxelReference() {
        return voxelBlob;
    }

    private void setVoxelReference(VoxelBlobStateReference voxel) {
        boolean hasVoxelBlob = voxelBlob != null;
        voxelBlob = voxel;
        requestModelDataUpdate();
        cachedShape = null;
        collisionShape = null;
        itemCache = null;
        recalculateShape();

        if(getWorld() != null && getWorld().isRemote)
            getChunk(getWorld()).update(this, hasVoxelBlob);
    }

    public boolean hasRenderTracker() {
        return renderTracker != null;
    }

    public VoxelNeighborRenderTracker getRenderTracker() {
        if (renderTracker == null) renderTracker = new VoxelNeighborRenderTracker(world, pos);
        return renderTracker;
    }

    /**
     * Get the item stack this tile entity would become when the block is broken.
     */
    public ItemStack getItemStack() {
        //We cache the item because apparently Waila spammed getPickBlock at some point in the past causing lagg. Caching can't hurt though.
        if(itemCache == null)
            itemCache = buildItemStack();
        return itemCache;
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        IModelData imd = super.getModelData();
        imd.setData(VOXEL_DATA, getVoxelReference());
        imd.setData(NEIGHBOUR_RENDER_TRACKER, getRenderTracker());
        return imd;
    }

    /**
     * Get the cached shape of this block.
     */
    @Nonnull
    public VoxelShape getCachedShape() {
        if (cachedShape == null) recalculateShape();
        return cachedShape == null ? VoxelShapes.empty() : cachedShape;
    }

    /**
     * Get the cached shape of this block.
     */
    @Nonnull
    public VoxelShape getCollisionShape() {
        if (ChiselUtil.ACTIVELY_TRACING) {
            //This will trigger if we're doing raytracing, and we need to do a custom shape to also have fluids included.
            VoxelShape base = VoxelShapes.empty();
            if (getVoxelReference() != null)
                for (AxisAlignedBB box : getVoxelReference().getInstance().getBoxes())
                    base = VoxelShapes.combine(base, VoxelShapes.create(box), IBooleanFunction.OR);
            return base.simplify();
        }
        if (collisionShape == null) recalculateShape();
        return collisionShape == null ? VoxelShapes.empty() : collisionShape;
    }

    /**
     * Recalculates the voxel shapes.
     */
    public void recalculateShape() {
        if (collisionShape == null) {
            VoxelShape base = VoxelShapes.empty();
            if (getVoxelReference() != null)
                for (AxisAlignedBB box : getVoxelReference().getInstance().getCollidableBoxes())
                    base = VoxelShapes.combine(base, VoxelShapes.create(box), IBooleanFunction.OR);
            collisionShape = base.simplify();
        }

        if (cachedShape == null && getVoxelReference() != null)
            cachedShape = VoxelShapes.create(getVoxelReference().getVoxelBlob().getBounds().toBoundingBox());
    }

    /**
     * Builds an item stack for a chiseled block.
     */
    public ItemStack buildItemStack() {
        VoxelBlob blob = getBlob();
        if(blob.filled() == 0)
            return ItemStack.EMPTY;

        final NBTBlobConverter c = new NBTBlobConverter();
        c.setBlob(blob);

        final CompoundNBT comp = new CompoundNBT();
        c.writeChiselData(comp);

        final ItemStack stack = new ItemStack(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK, 1);
        stack.setTagInfo(ChiselUtil.NBT_BLOCKENTITYTAG, comp);
        return stack;
    }

    /**
     * Get the tile chunk this block belongs to.
     */
    public TileChunk getChunk(final World world) {
        if (chunk == null) {
            chunk = findRenderChunk(getPos(), world, () -> new TileChunk(this));
            if(world.isRemote)
                chunk.register(this, true); //Register us to be a part of the chunk if this is the first time we're searching.
        }
        return chunk;
    }

    /**
     * Find the rendering chunk this TE belongs to.
     */
    public static TileChunk findRenderChunk(final BlockPos pos, final IBlockReader access, final Supplier<TileChunk> backup) {
        int chunkPosX = pos.getX() & TileChunk.CHUNK_COORDINATE_MASK;
        int chunkPosY = pos.getY() & TileChunk.CHUNK_COORDINATE_MASK;
        int chunkPosZ = pos.getZ() & TileChunk.CHUNK_COORDINATE_MASK;

        for (int x = 0; x < TileChunk.TILE_CHUNK_SIZE; ++x) {
            for (int y = 0; y < TileChunk.TILE_CHUNK_SIZE; ++y) {
                for (int z = 0; z < TileChunk.TILE_CHUNK_SIZE; ++z) {
                    final TileEntity te = access.getTileEntity(new BlockPos(chunkPosX + x, chunkPosY + y, chunkPosZ + z));
                    if (te instanceof ChiseledBlockTileEntity && ((ChiseledBlockTileEntity) te).chunk != null)
                        return ((ChiseledBlockTileEntity) te).chunk;
                }
            }
        }

        return backup.get();
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public boolean canRenderBreaking() {
        return true;
    }

    /**
     * Update this tile entity's voxel data.
     */
    public boolean updateBlob(final NBTBlobConverter converter) {
        final VoxelBlobStateReference originalRef = getVoxelReference();

        VoxelBlobStateReference voxelRef;
        try {
            voxelRef = converter.getVoxelRef(VoxelVersions.getDefault());
        } catch (final Exception e) {
            e.printStackTrace();
            voxelRef = new VoxelBlobStateReference();
        }

        setVoxelReference(voxelRef);
        return voxelRef == null || !voxelRef.equals(originalRef);
    }

    public VoxelBlob getBlob() {
        VoxelBlob vb = new VoxelBlob();
        final VoxelBlobStateReference vbs = getVoxelReference();

        if (vbs != null) vb = vbs.getVoxelBlob();
        else //If we can't make it proper we should return one made of air.
            vb.fill(VoxelBlob.AIR_BIT);

        return vb;
    }

    /**
     * Set the voxel blob to new data.
     */
    public void setBlob(final VoxelBlob vb) {
        setVoxelReference(new VoxelBlobStateReference(vb.write(VoxelVersions.getDefault())));
        setPrimaryBlock(vb.getMostCommonStateId()); //We only want this to every be a blockstate.
        markDirty();
        try {
            //Trigger block update
            if (!world.isRemote) {
                world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    public void completeEditOperation(final PlayerEntity player, final VoxelBlob vb, final boolean updateUndoTracker) {
        //Empty voxelblob = we need to destroy this block.
        if(vb.filled() <= 0) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
            return;
        }

        //Turn to full block if made of one type.
        int singleType = vb.singleType();
        if(singleType != VoxelBlob.AIR_BIT) {
            VoxelType type = VoxelType.getType(singleType);
            switch(type) {
                case BLOCKSTATE:
                    world.setBlockState(pos, BitUtil.getBlockState(singleType), 3);
                    return;
                case FLUIDSTATE:
                    world.setBlockState(pos, BitUtil.getFluidState(singleType).getBlockState(), 3);
                    return;
            }
        }

        if(updateUndoTracker) {
            final VoxelBlobStateReference before = getVoxelReference();
            setBlob(vb);
            final VoxelBlobStateReference after = getVoxelReference();

            ChiselsAndBits2.getInstance().getClient().getUndoTracker().add(player, getWorld(), getPos(), before, after);
        } else setBlob(vb);
    }

    public void fillWith(final int stateId) {
        setBlob(new VoxelBlob().fill(stateId));
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, -999, this.getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        read(pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(super.getUpdateTag());
    }

    @Override
    public CompoundNBT write(final CompoundNBT compound) {
        super.write(compound);
        NBTBlobConverter converter = new NBTBlobConverter(this);
        converter.writeChiselData(compound);
        return compound;
    }

    @Override
    public void read(final CompoundNBT compound) {
        super.read(compound);
        final NBTBlobConverter converter = new NBTBlobConverter(this);
        converter.readChiselData(compound, VoxelVersions.getDefault());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        detachRenderer();
    }

    @Override
    public void onChunkUnloaded() {
        detachRenderer();
    }

    @Override
    public void remove() {
        super.remove();
        detachRenderer();
    }

    private void detachRenderer() {
        if (chunk != null) {
            chunk.unregister(this, true);
            chunk = null;
        }
    }
}
