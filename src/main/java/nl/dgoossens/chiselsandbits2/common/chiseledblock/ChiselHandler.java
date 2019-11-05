package nl.dgoossens.chiselsandbits2.common.chiseledblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselTypeIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelRegionSrc;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.*;
import nl.dgoossens.chiselsandbits2.common.network.client.CChiselBlockPacket;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.InventoryUtils;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static nl.dgoossens.chiselsandbits2.api.BitOperation.*;

/**
 * Handles chiseling a block.
 */
public class ChiselHandler {
    /**
     * Handles an incoming {@link CChiselBlockPacket} packet.
     */
    public static void handle(final CChiselBlockPacket pkt, final PlayerEntity player) {
        if (!(player.getHeldItemMainhand().getItem() instanceof BitModifyItem))
            return; //Extra security, if you're somehow no longer holding a valid item that can chisel we cancel.

        final World world = player.world;
        final InventoryUtils.CalculatedInventory inventory = InventoryUtils.buildInventory(player);

        //Cancel chiseling early on if possible (but not if creative)
        if (inventory.getAvailableDurability() == 0 && !player.isCreative()) {
            player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.need_chisel"), true);
            return;
        }

        //If world.getServer() is null we return to make the "if (world.getServer().isBlockProtected(world, pos, player))" never fail.
        if (world.getServer() == null)
            return;

        final VoxelType type = VoxelType.getType(pkt.placedBit);
        final VoxelWrapper wrapper = VoxelWrapper.forAbstract(pkt.placedBit);
        final boolean isCreative = player.isCreative();

        final BlockPos from = pkt.from.blockPos;
        final BlockPos to = pkt.to.blockPos;

        final int minX = Math.min(from.getX(), to.getX());
        final int maxX = Math.max(from.getX(), to.getX());
        final int minY = Math.min(from.getY(), to.getY());
        final int maxY = Math.max(from.getY(), to.getY());
        final int minZ = Math.min(from.getZ(), to.getZ());
        final int maxZ = Math.max(from.getZ(), to.getZ());

        ChiselsAndBits2.getInstance().getClient().getUndoTracker().beginGroup(player);

        try {
            //Uses to be added to the statistic
            for (int xOff = minX; xOff <= maxX; ++xOff) {
                for (int yOff = minY; yOff <= maxY; ++yOff) {
                    for (int zOff = minZ; zOff <= maxZ; ++zOff) {
                        boolean changed = false;
                        final BlockPos pos = new BlockPos(xOff, yOff, zOff);
                        //If we can't chisel here, don't chisel.
                        //This method is specifically a server only method, the client already does checking if we can chisel somewhere through ChiselUtil#canChiselPosition.
                        if (world.getServer().isBlockProtected(world, pos, player))
                            continue;

                        //Replace the block with a chiseled block.
                        replaceWithChiseled(player, world, pos, world.getBlockState(pos), pkt.placedBit, pkt.side);

                        final TileEntity te = world.getTileEntity(pos);
                        if (te instanceof ChiseledBlockTileEntity) {
                            final ChiseledBlockTileEntity tec = (ChiseledBlockTileEntity) te;
                            final VoxelBlob vb = tec.getBlob();
                            final ChiselIterator i = getIterator(pkt, new VoxelRegionSrc(world, pos, 1), pos, pkt.operation);
                            final Map<Integer, Long> extracted = new HashMap<>();

                            //Handle the operation
                            switch (pkt.operation) {
                                case SWAP:
                                case PLACE: {
                                    //Determine our resources, not needed if this is a REMOVE operation
                                    long bitsUsed = 0, bitsAvailable = 0;
                                    //In creative you can use as many as you want.
                                    if (type == VoxelType.COLOURED || isCreative) {
                                        //You can use as many coloured bits as you want.
                                        bitsAvailable = Integer.MAX_VALUE;
                                    } else {
                                        //Count up all of the bits of our type we have in all of our storages
                                        for (ItemStack item : player.inventory.mainInventory) {
                                            if (item.getItem() instanceof StorageItem) {
                                                LazyOptional<BitStorage> cap = item.getCapability(StorageCapabilityProvider.STORAGE);
                                                if (cap.isPresent())
                                                    bitsAvailable += cap.orElse(null).get(wrapper);
                                            }
                                        }
                                    }

                                    //If we have no bits we can't place anything.
                                    if (bitsAvailable <= 0)
                                        return;

                                    while (i.hasNext()) {
                                        int blk = vb.get(i.x(), i.y(), i.z());

                                        //If this is a place operation we only place in air.
                                        if (pkt.operation == PLACE && (blk != VoxelBlob.AIR_BIT && !VoxelType.isFluid(blk)))
                                            continue;

                                        //Don't waste durability on the same type.
                                        if (pkt.operation == SWAP && blk == pkt.placedBit)
                                            continue;

                                        if (inventory.damageChisel()) {
                                            vb.set(i.x(), i.y(), i.z(), pkt.placedBit);
                                            changed = true;

                                            //Track how many bits we've extracted and it's not a coloured bit.
                                            if (pkt.operation == SWAP && !VoxelType.isColoured(blk))
                                                extracted.put(blk, extracted.getOrDefault(blk, 0L) + 1);

                                            //Test resources
                                            bitsUsed += 1;
                                            if (bitsUsed >= bitsAvailable)
                                                break;
                                        } else break; //If damage chisel is false once it will never become true again, so this break saves time.
                                    }

                                    //Take the used resources, coloured bits don't take resources.
                                    if (!isCreative && type != VoxelType.COLOURED) {
                                        //Iterate over every bag.
                                        for (ItemStack item : player.inventory.mainInventory) {
                                            if (item.getItem() instanceof StorageItem) {
                                                LazyOptional<BitStorage> cap = item.getCapability(StorageCapabilityProvider.STORAGE);
                                                if (cap.isPresent()) {
                                                    BitStorage bs = cap.orElse(null);
                                                    if (bs.has(wrapper)) {
                                                        bitsUsed = -bs.add(wrapper, -bitsUsed);
                                                        ChiselModeManager.updateStackCapability(item, bs, player);
                                                    }

                                                    //We can break early if we're done.
                                                    if (bitsUsed <= 0)
                                                        break;
                                                }
                                            }
                                        }

                                        if (bitsUsed > 0)
                                            throw new RuntimeException("Player didn't pay off resource debt, how did this happen?");
                                    }
                                }
                                break;
                                case REMOVE: {
                                    while (i.hasNext()) {
                                        final int blk = vb.get(i.x(), i.y(), i.z());
                                        if (blk == VoxelBlob.AIR_BIT)
                                            continue; //We don't need to remove air bits.

                                        if (inventory.damageChisel()) {
                                            vb.clear(i.x(), i.y(), i.z());
                                            changed = true;

                                            //Track how many bits we've extracted and it's not a coloured bit.
                                            if (!VoxelType.isColoured(blk))
                                                extracted.put(blk, extracted.getOrDefault(blk, 0L) + 1);
                                        } else break; //If damage chisel is false once it will never become true again, so this break saves time.
                                    }
                                }
                                break;
                            }

                            //Give the player the bits that were extracted
                            for (int extr : extracted.keySet()) {
                                long toGive = extracted.get(extr);
                                VoxelType vt = VoxelType.getType(extr);
                                if (vt != VoxelType.BLOCKSTATE && vt != VoxelType.FLUIDSTATE)
                                    continue;

                                VoxelWrapper w = VoxelWrapper.forAbstract(extr);

                                //First round: find storages that already want the bits
                                for (ItemStack item : player.inventory.mainInventory) {
                                    if (item.getItem() instanceof StorageItem) {
                                        LazyOptional<BitStorage> cap = item.getCapability(StorageCapabilityProvider.STORAGE);
                                        if (cap.isPresent()) {
                                            BitStorage bs = cap.orElse(null);
                                            if (bs.has(w)) {
                                                long h = Math.min(toGive, bs.queryRoom(w));
                                                bs.add(w, h);
                                                toGive -= h;
                                                ChiselModeManager.updateStackCapability(item, bs, player);
                                            }
                                        }
                                    }
                                }

                                if (toGive <= 0)
                                    continue;

                                //Second round: put the bits in the first possible storage
                                for (ItemStack item : player.inventory.mainInventory) {
                                    if (item.getItem() instanceof StorageItem) {
                                        LazyOptional<BitStorage> cap = item.getCapability(StorageCapabilityProvider.STORAGE);
                                        if (cap.isPresent()) {
                                            BitStorage bs = cap.orElse(null);
                                            long h = Math.min(toGive, bs.queryRoom(w));
                                            bs.add(w, h);
                                            toGive -= h;
                                            ChiselModeManager.updateStackCapability(item, bs, player);
                                        }
                                    }

                                    //If we've deposited everything we're done.
                                    if (toGive <= 0)
                                        break;
                                }
                            }

                            //Actually apply the operation.
                            tec.completeEditOperation(vb);

                            //Send the breaking sound only if the block actually got changed.
                            if (changed) {
                                SoundType st = world.getBlockState(pos).getSoundType();
                                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, st.getBreakSound(), SoundCategory.BLOCKS, (st.getVolume() + 1.0F) / 16.0F, st.getPitch() * 0.9F);
                            }
                        }
                    }
                }
            }
        } finally {
            inventory.apply();
            ChiselsAndBits2.getInstance().getClient().getUndoTracker().endGroup(player);
        }
    }

    /**
     * Get the selected item mode.
     */
    public static SelectedItemMode getSelectedMode() {
        long stamp = 0;

        for (ItemStack item : Minecraft.getInstance().player.inventory.mainInventory) {
            if (item.getItem() instanceof StorageItem) {
                long l = ChiselModeManager.getSelectionTime(item);
                if (l > stamp) {
                    stamp = l;
                    return ChiselModeManager.getSelectedItem(item);
                }
            }
        }
        //Default is the empty bag slot.
        return SelectedItemMode.NONE;
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

    public static void replaceWithChiseled(final @Nonnull PlayerEntity player, final @Nonnull World world, final @Nonnull BlockPos pos, final BlockState originalState, final int fragmentBlockStateID, final Direction face) {
        Block target = originalState.getBlock();
        if(target.equals(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK)) return;

        IFluidState fluid = world.getFluidState(pos);
        boolean isAir = isBlockReplaceable(player, world, pos, face, true);

        if (ChiselUtil.canChiselBlock(originalState) || isAir) {
            int blockId = isAir ? fragmentBlockStateID : ModUtil.getStateId(originalState);
            world.setBlockState(pos, ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK.getDefaultState(), 3);
            final ChiseledBlockTileEntity te = (ChiseledBlockTileEntity) world.getTileEntity(pos);
            if (te != null) {
                if (!isAir) te.fillWith(blockId);
                else {
                    //If there was a fluid previously make this a fluid block instead of an air block.
                    if (fluid.isEmpty()) te.fillWith(VoxelBlob.AIR_BIT);
                    else te.fillWith(ModUtil.getFluidId(fluid));
                }
            }
        }
    }

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

    //Interfaces to use to designate which items can place or remove bits.
    public static interface BitPlaceItem extends BitModifyItem {}
    public static interface BitRemoveItem extends BitModifyItem {}
    public static interface BitModifyItem {}
}
