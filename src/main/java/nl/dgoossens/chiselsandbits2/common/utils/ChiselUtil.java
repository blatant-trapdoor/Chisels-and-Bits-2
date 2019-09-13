package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.event.ForgeEventFactory;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.culling.DummyEnvironmentWorldReader;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;
import nl.dgoossens.chiselsandbits2.api.VoxelType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChiselUtil {
    private static Map<Block, Boolean> supportedBlocks = new ConcurrentHashMap<>();

    /**
     * Checks whether or not a given block can be chiseled.
     */
    public static boolean canChiselBlock(final BlockState block) {
        if(block.getBlock() instanceof ChiseledBlock) return true;
        if(!supportedBlocks.containsKey(block.getBlock())) testBlock(block);
        return supportedBlocks.getOrDefault(block.getBlock(), false);
    }

    /**
     * Tests if a block can be turned into a ChiseledBlock, this works by
     * looking at the VoxelShape and testing if it's made of 1/16th's of the
     * block.
     */
    private static void testBlock(final BlockState block) {
        //We determine if a block can be chiseled by whether or not the shape of it can be turned into a VoxelBlob.
        final Block blk = block.getBlock();
        if(blk.hasTileEntity(block)) return;
        DummyEnvironmentWorldReader dummyWorld = new DummyEnvironmentWorldReader() {
            @Override
            public BlockState getBlockState(BlockPos pos) {
                if(pos.equals(BlockPos.ZERO)) return block;
                return super.getBlockState(pos);
            }
        };
        VoxelShape shape = block.getCollisionShape(dummyWorld, BlockPos.ZERO);
        if(!shape.equals(VoxelShapes.fullCube())) return; //You can only chisel blocks without a special shape.
        supportedBlocks.put(blk, true);
    }

    /**
     * Returns whether or not this block at this position can be chiseled, uses an destroy block
     * event.
     */
    public static boolean canChiselPosition(final BlockPos pos, final PlayerEntity player, final BlockState state, final Direction face) {
        if(!player.getHeldItemMainhand().getItem().equals(ChiselsAndBits2.getItems().CHISEL)) return false; //The chisel needs to be in the main hand!
        if(!player.getEntityWorld().getWorldBorder().contains(pos)) return false;
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
     * @deprecated Just use {@link VoxelType#getType(int)}
     */
    @Deprecated
    public static VoxelType getTypeFromStateID(final int bit) { return VoxelType.getType(bit); }
}
