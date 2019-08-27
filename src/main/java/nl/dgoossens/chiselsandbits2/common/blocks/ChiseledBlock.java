package nl.dgoossens.chiselsandbits2.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.items.ChiseledBlockItem;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChiseledBlock extends Block implements BaseBlock {
    public ChiseledBlock(Properties properties) { super(properties); }

    @Override
    public BlockItem getBlockItem() {
        return new ChiseledBlockItem(this, new Item.Properties());
    }
    @Override
    public boolean hasTileEntity(BlockState state) { return true; }
    @Override //Required for getting the destroyStage in the TER.
    public boolean hasCustomBreakingProgress(BlockState state) { return true; }
    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) { return new ChiseledBlockTileEntity(); }
    @Override //Our rendering shape is identical to the collision shape.
    public VoxelShape getRenderShape(BlockState state, IBlockReader worldIn, BlockPos pos) { return getCollisionShape(state, worldIn, pos, ISelectionContext.dummy()); }
    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(!(te instanceof ChiseledBlockTileEntity)) return VoxelShapes.empty();
        else return ((ChiseledBlockTileEntity) te).getCollisionShape();
    }
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(!(te instanceof ChiseledBlockTileEntity)) return VoxelShapes.empty(); //TODO really need to make this a util especially with multipart
        else return ((ChiseledBlockTileEntity) te).getCachedShape();
    }
    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer) { return true; }
    @Override
    public BlockRenderType getRenderType(BlockState state) { return BlockRenderType.ENTITYBLOCK_ANIMATED; }

    /**
     * Get the blockstate of the block that this chiseled block
     * is mainly made of.
     */
    public BlockState getPrimaryState(IBlockReader world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(te==null) return null;
        return ModUtil.getStateById(((ChiseledBlockTileEntity) te).getPrimaryBlock());
    }

    /*@Override
    public IBlockSlot getSlot(BlockState state) { return ChiselsAndBits2.getAPI().getChiselsAndBitsSlot(); }*/

    public static class ReplaceWithChiseledValue {
        public boolean success = false;
        public ChiseledBlockTileEntity te = null;
    }

    public static ReplaceWithChiseledValue replaceWithChiseled(
            final @Nonnull World world,
            final @Nonnull BlockPos pos,
            final BlockState originalState,
            final int fragmentBlockStateID,
            final boolean triggerUpdate )
    {
        BlockState actingState = originalState;
        Block target = originalState.getBlock();
        final boolean isAir = world.isAirBlock( pos ); //TODO add isReplacable for grass or smth
        ReplaceWithChiseledValue rv = new ReplaceWithChiseledValue();

        if ( ChiselUtil.canChiselBlock( actingState ) || isAir )
        {
            Block blk = ChiselsAndBits2.getBlocks().CHISELED_BLOCK;

            int BlockID = ModUtil.getStateId( actingState );

            if ( isAir )
            {
                actingState = ModUtil.getStateById( fragmentBlockStateID );
                target = actingState.getBlock();
                BlockID = ModUtil.getStateId( actingState );
                // its still air tho..
                actingState = Blocks.AIR.getDefaultState();
            }

            if ( BlockID == 0 )
            {
                return rv;
            }

            if ( blk != null && blk != target )
            {
                world.setBlockState( pos, blk.getDefaultState(), triggerUpdate ? 3 : 0 );
                final TileEntity te = world.getTileEntity( pos );

                ChiseledBlockTileEntity tec;
                if ( !( te instanceof ChiseledBlockTileEntity ) )
                {
                    tec = (ChiseledBlockTileEntity) blk.createTileEntity( blk.getDefaultState(), world );
                    world.setTileEntity( pos, tec );
                }
                else
                {
                    tec = (ChiseledBlockTileEntity) te;
                }

                if ( tec != null )
                {
                    tec.setPrimaryBlock(BlockID);
                    tec.fillWith( actingState );
                }

                rv.success = true;
                rv.te = tec;

                return rv;
            }
        }

        return rv;
    }
}
