package nl.dgoossens.chiselsandbits2.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import nl.dgoossens.chiselsandbits2.common.items.ChiseledBlockItem;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;
import java.util.Optional;

public class ChiseledBlock extends Block implements BaseBlock {
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

    @Override
    public SoundType getSoundType(BlockState state, IWorldReader world, BlockPos pos, @Nullable Entity entity) {
        return getPrimaryState(world, pos).getSoundType(world, pos, entity);
    }

    @Override
    public boolean isSolid(BlockState state) {
        return false; //We say it's never solid to avoid shouldSideBeRendered from returning false somehow.
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
}
