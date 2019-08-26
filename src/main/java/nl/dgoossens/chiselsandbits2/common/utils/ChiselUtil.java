package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.culling.DummyEnvironmentWorldReader;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChiselUtil {
    private static Set<Block> testedBlocks = new CopyOnWriteArraySet<>();
    private static Map<Block, VoxelBlob> defaultShapes = new ConcurrentHashMap<>();
    private static Set<Integer> fluidStates = new CopyOnWriteArraySet<>();

    /**
     * Checks whether or not a given block can be chiseled.
     */
    public static boolean canChiselBlock(final BlockState block) {
        if(block.getBlock() instanceof ChiseledBlock) return true;
        if(!testedBlocks.contains(block.getBlock())) testBlock(block);
        return defaultShapes.containsKey(block.getBlock());
    }

    /**
     * Tests if a block can be turned into a ChiseledBlock, this works by
     * looking at the VoxelShape and testing if it's made of 1/16th's of the
     * block.
     */
    private static void testBlock(final BlockState block) {
        //We determine if a block can be chiseled by whether or not the shape of it can be turned into a VoxelBlob.
        final Block blk = block.getBlock();
        if(blk.hasTileEntity(block)) return; //TODO support tile entities!
        DummyEnvironmentWorldReader dummyWorld = new DummyEnvironmentWorldReader() {
            @Override
            public BlockState getBlockState(BlockPos pos) {
                if(pos.equals(BlockPos.ZERO)) return block;
                return super.getBlockState(pos);
            }
        };
        VoxelShape shape = block.getCollisionShape(dummyWorld, BlockPos.ZERO);
        shape.simplify();
        for(AxisAlignedBB bb : shape.toBoundingBoxList()) {
            if(bb.getXSize() * 16 % 1 != 0) return; //If the bounding boxes are all made of parts of 1/16th it should be usable.
            if(bb.getYSize() * 16 % 1 != 0) return;
            if(bb.getZSize() * 16 % 1 != 0) return;
        }
        defaultShapes.put(blk, VoxelBlob.shape(block, shape));
    }

    /**
     * Get the voxelblob to use as a shape when the blockstate is
     * turned into a chiseled block.
     */
    public static VoxelBlob getShape(final BlockState block) {
        return defaultShapes.getOrDefault(block.getBlock(), VoxelBlob.full(block)).clone();
    }

    /**
     * Returns whether or not this block at this position can be chiseled, uses an destroy block
     * event.
     */
    public static boolean canChiselPosition(final BlockPos pos, final PlayerEntity player, final BlockState state, final Direction face) {
        if(!player.getHeldItemMainhand().getItem().equals(ChiselsAndBits2.getItems().CHISEL)) return false; //The chisel needs to be in the main hand!
        if(!player.getEntityWorld().isBlockModifiable(player, pos)) return false;
        if(!player.canPlayerEdit(pos, face, player.getHeldItemMainhand())) return false;
        return ForgeEventFactory.onEntityDestroyBlock(player, pos, state);
    }

    /**
     * Check if a block at a given position is already chiseled.
     */
    public static boolean isBlockChiseled(final BlockPos position, final IBlockReader world) {
        return world.getBlockState(position).getBlock() instanceof ChiseledBlock;
    }

    /**
     * Get the voxel type of an given blockstate's id.
     */
    public static VoxelType getTypeFromStateID(final int bit) {
        if(bit == 0) return VoxelType.AIR;
        return fluidStates.contains(bit) ? VoxelType.FLUID : VoxelType.SOLID;
    }
}
