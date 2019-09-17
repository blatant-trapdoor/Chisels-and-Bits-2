package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChiselUtil {
    private static Map<Block, Boolean> supportedBlocks = new ConcurrentHashMap<>();

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
        VoxelShape shape = block.getCollisionShape(dummyWorld, BlockPos.ZERO);
        if (!shape.equals(VoxelShapes.fullCube())) return; //You can only chisel blocks without a special shape.
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
        return entity.world.rayTraceBlocks(new RayTraceContext(vec3d, vec3d2, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
    }
}
