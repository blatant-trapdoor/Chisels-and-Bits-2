package nl.dgoossens.chiselsandbits2.common.blocks;

import net.minecraft.block.*;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.Property;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.items.ChiseledBlockItem;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.LongAdder;

public class ChiseledBlock extends Block implements BaseBlock {
    public static final ResourceLocation field_220169_b = new ResourceLocation("block_drop");

    public ChiseledBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockItem getBlockItem() {
        return new ChiseledBlockItem(this, new Item.Properties());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override //Required for getting the destroyStage in the TER.
    public boolean hasCustomBreakingProgress(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ChiseledBlockTileEntity();
    }

    @Override //Our rendering shape is identical to the collision shape.
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return getCollisionShape(state, worldIn, pos, ISelectionContext.dummy());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof ChiseledBlockTileEntity)) return VoxelShapes.empty();
        else return ((ChiseledBlockTileEntity) te).getCollisionShape();
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof ChiseledBlockTileEntity)) return VoxelShapes.empty();
        else return ((ChiseledBlockTileEntity) te).getCachedShape();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED; //Set it to TESR only mode so there's no normal model.
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) {
        return true;
    }

    //Redirect getSoundType to the primary block.
    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return getPrimaryState(world, pos).getSoundType(world, pos, entity);
    }

    //Redirect getSlipperiness to the primary block.
    @Override
    public float getSlipperiness(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return getPrimaryState(world, pos).getSlipperiness(world, pos, entity);
    }

    //TODO make solid depending on a kind of fullBlock value just like C&B1, saves performance when you have large amounts of full block mixed blocks.
    @Override
    public boolean isSolid(BlockState state) {
        return false; //We say it's never solid to avoid shouldSideBeRendered from returning false somehow.
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        try {
            VoxelBlob tar = ((ChiseledBlockTileEntity) worldserver.getTileEntity(pos)).getVoxelReference().getVoxelBlob();
            BitLocation location = new BitLocation(entity);
            return spawnParticle(tar.getSafe(location.bitX, location.bitY, location.bitZ), worldserver, pos, entity, numberOfParticles);
        } catch(Exception x) {}
        return false;
    }

    private boolean spawnParticle(int i, ServerWorld worldserver, BlockPos pos, LivingEntity entity, int numberOfParticles) {
        switch(VoxelType.getType(i)) {
            case BLOCKSTATE:
                worldserver.spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, ModUtil.getBlockState(i)), entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0, 0.0, 0.0, 1);
                return true;
            case COLOURED:
                Color c = ModUtil.getColourState(i);
                worldserver.spawnParticle(new RedstoneParticleData(((float)c.getRed())/255.0f, ((float)c.getGreen())/255.0f, ((float)c.getBlue())/255.0f, ((float)c.getAlpha())/255.0f), entity.posX, entity.posY, entity.posZ, numberOfParticles, 0.0, 0.0, 0.0, 1);
                return true;
        }
        return false;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return false; //TODO add more effect compat
    }

    @Override
    public boolean addHitEffects(BlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        return false;
    }

    @Override
    public boolean addRunningEffects(BlockState state, World world, BlockPos pos, Entity entity) {
        return false;
    }

    /**
     * Get the blockstate of the block that this chiseled block
     * is mainly made of.
     */
    public BlockState getPrimaryState(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null) return null;
        return ModUtil.getBlockState(((ChiseledBlockTileEntity) te).getPrimaryBlock());
    }

    /*@Override
    public IBlockSlot getSlot(BlockState state) { return ChiselsAndBits2.getAPI().getChiselsAndBitsSlot(); }*/

    //-- HANDLE BLOCK DROPS, COPIED FROM BLOCKSHULKERBOX.JAVA ---
    /**
     * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
     * this block.
     */
    public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
        if(player.isCreative()) {
            super.onBlockHarvested(worldIn, pos, state, player);
            return;
        }
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof ChiseledBlockTileEntity) {
            ChiseledBlockTileEntity cte = (ChiseledBlockTileEntity) tileentity;
            ItemEntity itementity = new ItemEntity(worldIn, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), cte.getItemStack());
            itementity.setDefaultPickupDelay();
            worldIn.addEntity(itementity);
        }

        super.onBlockHarvested(worldIn, pos, state, player);
    }

    /**
     * Handle block dropping properly from alternative sources, e.g. explosions.
     */
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        TileEntity tileentity = builder.get(LootParameters.BLOCK_ENTITY);
        if (tileentity instanceof ChiseledBlockTileEntity)
            builder = builder.withDynamicDrop(field_220169_b, (p_220168_1_, p_220168_2_) -> {
                ((ChiseledBlockTileEntity) tileentity).getItemStack();
            });
        return super.getDrops(state, builder);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof ChiseledBlockTileEntity)
            return ((ChiseledBlockTileEntity) te).getItemStack();
        return ItemStack.EMPTY;
    }
}
