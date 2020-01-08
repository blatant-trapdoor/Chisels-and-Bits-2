package nl.dgoossens.chiselsandbits2.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.block.BitOperation;
import nl.dgoossens.chiselsandbits2.api.block.IVoxelSrc;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IBitModifyItem;
import nl.dgoossens.chiselsandbits2.common.impl.item.ItemMode;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.chisel.ChiselIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.chisel.ChiselTypeIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.network.client.CChiselBlockPacket;

import javax.annotation.Nonnull;

import static nl.dgoossens.chiselsandbits2.api.block.BitOperation.PLACE;

/**
 * A util that handles everything related to the chiseleling process with some extra
 * general utility methods.
 */
public class ChiselUtil {
    public static boolean ACTIVELY_TRACING = false;
    public static final String NBT_BLOCKENTITYTAG = "BlockEntityTag";
    private static long memory = -1; //The amount of memory allocated to Minecraft.

    /**
     * Returns true if less than 1256 MB in memory is allocated
     * to Minecraft.
     */
    public static boolean isLowMemoryMode() {
        if (memory == -1)
            memory = Runtime.getRuntime().maxMemory() / (1024 * 1024); // mb
        return memory < 1256;
    }

    /**
     * Plays the sound to be played when a chiseled block is modified.
     */
    public static void playModificationSound(final World world, final BlockPos pos, final boolean placement) {
        BlockState state = getChiseledTileMainState(world, pos);
        playModificationSound(world, pos, placement, state);
    }

    /**
     * Internal utility method for calculating what the primary blockstate of a block
     * is.
     */
    public static BlockState getChiseledTileMainState(final World world, final BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if(world.getTileEntity(pos) instanceof ChiseledBlockTileEntity) {
            int a = ((ChiseledBlockTileEntity) world.getTileEntity(pos)).getPrimaryBlock();
            if(a != VoxelBlob.AIR_BIT) state = BitUtil.getBlockState(a);
        }
        return state;
    }

    /**
     * Plays the sound to be played when a chiseled block is modified with a set state.
     */
    public static void playModificationSound(final World world, final BlockPos pos, final boolean placement, final BlockState state) {
        SoundType st = state.getSoundType();
        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, placement ? st.getPlaceSound() : st.getBreakSound(), SoundCategory.BLOCKS, st.getVolume(), st.getPitch() * 0.9F);
    }

    /**
     * Returns whether or not this block at this position can be chiseled, uses an destroy block
     * event.
     */
    public static boolean canChiselPosition(final BlockPos pos, final PlayerEntity player, final BlockState state, final Direction face) {
        if (!(player.getHeldItemMainhand().getItem() instanceof IBitModifyItem))
            return false; //A valid item needs to be in the main hand!
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

    /**
     * Returns whether a target block is air or can be replaced.
     */
    public static boolean isBlockReplaceable(final PlayerEntity player, final World world, final BlockPos pos, final Direction face, final boolean destroy) {
        boolean isValid = world.isAirBlock(pos);

        //We see it as air if we can replace it.
        if (!isValid && world.getBlockState(pos).isReplaceable(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d((double) pos.getX() + 0.5D + (double) face.getXOffset() * 0.5D, (double) pos.getY() + 0.5D + (double) face.getYOffset() * 0.5D, (double) pos.getZ() + 0.5D + (double) face.getZOffset() * 0.5D), face, pos, false))))) {
            if (destroy) world.destroyBlock(pos, true);
            isValid = true;
        }

        return isValid;
    }

    /**
     * Replaces a block at a position with a new chiseled block tile entity.
     */
    public static void replaceWithChiseled(final @Nonnull PlayerEntity player, final @Nonnull World world, final @Nonnull BlockPos pos, final BlockState originalState, final Direction face) {
        Block target = originalState.getBlock();
        BlockState placementState = ChiselsAndBits2.getInstance().getAPI().getRestrictions().getPlacementState(originalState);
        if(target.equals(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK) || placementState == null) return;

        IFluidState fluid = world.getFluidState(pos);
        boolean isAir = isBlockReplaceable(player, world, pos, face, true);

        if (ChiselsAndBits2.getInstance().getAPI().getRestrictions().canChiselBlock(originalState) || isAir) {
            int blockId = BitUtil.getBlockId(placementState);
            world.setBlockState(pos, ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK.getDefaultState(), 3);
            final ChiseledBlockTileEntity te = (ChiseledBlockTileEntity) world.getTileEntity(pos);
            if (te != null) {
                if (!isAir) te.fillWith(blockId);
                else {
                    //If there was a fluid previously make this a fluid block instead of an air block.
                    if (fluid.isEmpty()) te.fillWith(VoxelBlob.AIR_BIT);
                    else te.fillWith(BitUtil.getFluidId(fluid));
                }
            }
        }
    }

    /**
     * Get the chisel iterator from a incoming chisel packet.
     */
    public static ChiselIterator getIterator(final CChiselBlockPacket pkt, final IVoxelSrc vb, final BlockPos pos, final BitOperation place) {
        if (pkt.mode == ItemMode.CHISEL_DRAWN_REGION) {
            final BlockPos from = pkt.from.blockPos;
            final BlockPos to = pkt.to.blockPos;

            final int bitX = pos.getX() == from.getX() ? pkt.from.bitX : 0;
            final int bitY = pos.getY() == from.getY() ? pkt.from.bitY : 0;
            final int bitZ = pos.getZ() == from.getZ() ? pkt.from.bitZ : 0;

            final int scaleX = (pos.getX() == to.getX() ? pkt.to.bitX : 15) - bitX + 1;
            final int scaleY = (pos.getY() == to.getY() ? pkt.to.bitY : 15) - bitY + 1;
            final int scaleZ = (pos.getZ() == to.getZ() ? pkt.to.bitZ : 15) - bitZ + 1;

            return new ChiselTypeIterator(VoxelBlob.DIMENSION, bitX, bitY, bitZ, scaleX, scaleY, scaleZ, pkt.side);
        }
        return ChiselTypeIterator.create(VoxelBlob.DIMENSION, pkt.from.bitX, pkt.from.bitY, pkt.from.bitZ, vb, pkt.mode, pkt.side, place.equals(PLACE));
    }
}
