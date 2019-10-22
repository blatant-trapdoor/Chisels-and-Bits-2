package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.event.ForgeEventFactory;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.culling.DummyEnvironmentWorldReader;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChiselUtil {
    private static Map<Block, Boolean> supportedBlocks = new ConcurrentHashMap<>();
    private static Set<Block> testedBlocks = new HashSet<>();
    public static boolean ACTIVELY_TRACING = false;

    /**
     * Checks whether or not a given block can be chiseled.
     */
    public static boolean canChiselBlock(final BlockState block) {
        if (!supportedBlocks.containsKey(block.getBlock())) testBlock(block);
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
        //Don't test twice!
        if(testedBlocks.contains(blk)) return;
        testedBlocks.add(blk);

        if (blk instanceof ChiseledBlock) {
            supportedBlocks.put(blk, true);
            return;
        }
        if (blk.hasTileEntity(block)) return;

        DummyEnvironmentWorldReader dummyWorld = new DummyEnvironmentWorldReader() {
            @Override
            public BlockState getBlockState(BlockPos pos) {
                if (pos.equals(BlockPos.ZERO)) return block;
                return super.getBlockState(pos);
            }
        };
        if (block.getBlockHardness(dummyWorld, BlockPos.ZERO) < 0) return; //Can't break unbreakable blocks. (they have -1 hardness)
        if (!block.getCollisionShape(dummyWorld, BlockPos.ZERO).equals(VoxelShapes.fullCube())) return; //You can only chisel blocks without a special shape.
        supportedBlocks.put(blk, true);
    }

    /**
     * Returns whether or not this block at this position can be chiseled, uses an destroy block
     * event.
     */
    public static boolean canChiselPosition(final BlockPos pos, final PlayerEntity player, final BlockState state, final Direction face) {
        if (!player.getHeldItemMainhand().getItem().equals(ChiselsAndBits2.getInstance().getItems().CHISEL))
            return false; //The chisel needs to be in the main hand!
        if (!player.getEntityWorld().getWorldBorder().contains(pos)) return false;
        if (!player.getEntityWorld().isBlockModifiable(player, pos)) return false;
        if (!player.canPlayerEdit(pos, face, player.getHeldItemMainhand())) return false;
        return ForgeEventFactory.onEntityDestroyBlock(player, pos, state);
    }

    /**
     * Raytraces from the player's eyes to a block, uses COLLIDER mode.
     */
    public static RayTraceResult rayTrace(final Entity entity) {
        Vec3d vec3d = entity.getEyePosition(1.0f);
        Vec3d vec3d1 = entity.getLook(1.0f);
        double d = (double) Minecraft.getInstance().playerController.getBlockReachDistance();
        Vec3d vec3d2 = vec3d.add(vec3d1.x * d, vec3d1.y * d, vec3d1.z * d);
        ACTIVELY_TRACING = true;
        RayTraceResult r = entity.world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
        ACTIVELY_TRACING = false;
        return r;
    }
}
