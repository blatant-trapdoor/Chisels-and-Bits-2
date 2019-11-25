package nl.dgoossens.chiselsandbits2.common.chiseledblock;

import net.minecraft.block.SoundType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelRegionSrc;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.network.client.CChiselBlockPacket;
import nl.dgoossens.chiselsandbits2.common.utils.InventoryUtils;

import static nl.dgoossens.chiselsandbits2.api.BitOperation.*;

/**
 * Handles chiseling a block.
 */
public class ChiselHandler {
    /**
     * Handles an incoming {@link CChiselBlockPacket} packet.
     */
    public static void handle(final CChiselBlockPacket pkt, final ServerPlayerEntity player) {
        if (!(player.getHeldItemMainhand().getItem() instanceof ChiselUtil.BitModifyItem))
            return; //Extra security, if you're somehow no longer holding a valid item that can chisel we cancel.

        final World world = player.world;
        final InventoryUtils.CalculatedInventory inventory = InventoryUtils.buildInventory(player);

        //Cancel chiseling early on if possible (but not if creative)
        if (inventory.getAvailableDurability() <= 0 && !player.isCreative()) {
            player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.need_chisel"), true);
            return;
        }

        final VoxelWrapper wrapper = VoxelWrapper.forAbstract(pkt.placedBit);

        //Placement/swap which means we need to track extracted bits
        if(!pkt.operation.equals(REMOVE)) {
            inventory.trackMaterialUsage(wrapper);

            //Cancel chiseling early on if possible (but not if creative)
            //This shouldn't ever happen because you can't have a material selected that you don't have.
            //Well actually this can happen if you drop the bags and instantly click before the routine updating catches up to you. tl;dr this can only happen if the cached value isn't updated on time
            if (inventory.getAvailableMaterial() <= 0 && !player.isCreative()) {
                player.sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.need_bits"), true);
                return;
            }
        }

        //If world.getServer() is null we return to make the "if (world.getServer().isBlockProtected(world, pos, player))" never fail.
        if (world.getServer() == null)
            return;

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
                        ChiselUtil.replaceWithChiseled(player, world, pos, world.getBlockState(pos), pkt.placedBit, pkt.side);

                        final TileEntity te = world.getTileEntity(pos);
                        if (te instanceof ChiseledBlockTileEntity) {
                            final ChiseledBlockTileEntity tec = (ChiseledBlockTileEntity) te;
                            final VoxelBlob vb = tec.getBlob();
                            final ChiselIterator i = ChiselUtil.getIterator(pkt, new VoxelRegionSrc(world, pos, 1), pos, pkt.operation);

                            //Handle the operation
                            switch (pkt.operation) {
                                case SWAP:
                                case PLACE: {
                                    while (i.hasNext()) {
                                        final int blk = vb.get(i.x(), i.y(), i.z());

                                        //If this is a place operation we only place in air.
                                        if (pkt.operation == PLACE && (blk != VoxelBlob.AIR_BIT && !VoxelType.isFluid(blk)))
                                            continue;

                                        //Don't waste durability on the same type.
                                        if (pkt.operation == SWAP && blk == pkt.placedBit)
                                            continue;

                                        if (inventory.hasMaterial() && inventory.damageChisel()) {
                                            //These two ifs need to be separate so one can fail but the other doesn't have to.
                                            //However we check hasMaterial early to ensure that this one will succeed true to avoid damage loss.
                                            if(inventory.canPlaceBit()) {
                                                vb.set(i.x(), i.y(), i.z(), pkt.placedBit);
                                                changed = true;

                                                //Track how many bits we've extracted and it's not a coloured bit.
                                                if (pkt.operation == SWAP)
                                                    inventory.addMaterial(VoxelWrapper.forAbstract(blk), 1);
                                            } else break; //If can place bit is false once it will never become true again, so this break saves time.
                                        } else break; //If damage chisel is false once it will never become true again, so this break saves time.
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

                                            inventory.addMaterial(VoxelWrapper.forAbstract(blk), 1);
                                        } else break; //If damage chisel is false once it will never become true again, so this break saves time.
                                    }
                                }
                                break;
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
}
