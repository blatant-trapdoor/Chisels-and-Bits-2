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
        ItemStack chisel = player.getHeldItemMainhand();
        if (!(chisel.getItem() instanceof BitModifyItem))
            return; //Extra security, if you're somehow no longer holding a chisel we cancel.

        final World world = player.world;

        //If world.getServer() is null we return to make the "if (world.getServer().isBlockProtected(world, pos, player))" never fail.
        if (world.getServer() == null)
            return;

        //Determine the placed bit, if this is REMOVE we set this to -1 to bypass any checks.
        int placeStateID = pkt.operation == REMOVE ? -1 : getSelectedBit(player, null);

        //If we couldn't find a selected type, don't chisel.
        if (placeStateID == VoxelBlob.AIR_BIT)
            return;

        final VoxelType type = VoxelType.getType(placeStateID);
        final VoxelWrapper wrapper = VoxelWrapper.forAbstract(placeStateID);
        final boolean isCreative = player.isCreative();

        final BlockPos from = pkt.from.blockPos;
        final BlockPos to = pkt.to.blockPos;

        final int minX = Math.min(from.getX(), to.getX());
        final int maxX = Math.max(from.getX(), to.getX());
        final int minY = Math.min(from.getY(), to.getY());
        final int maxY = Math.max(from.getY(), to.getY());
        final int minZ = Math.min(from.getZ(), to.getZ());
        final int maxZ = Math.max(from.getZ(), to.getZ());

        //TODO UndoTracker.getInstance().beginGroup( who );

        try {
            for (int xOff = minX; xOff <= maxX; ++xOff) {
                for (int yOff = minY; yOff <= maxY; ++yOff) {
                    for (int zOff = minZ; zOff <= maxZ; ++zOff) {

                        final BlockPos pos = new BlockPos(xOff, yOff, zOff);
                        //If we can't chisel here, don't chisel.
                        //This method is specifically a server only method, the client already does checking if we can chisel somewhere through ChiselUtil#canChiselPosition.
                        if (world.getServer().isBlockProtected(world, pos, player))
                            continue;

                        //Replace the block with a chiseled block.
                        replaceWithChiseled(player, world, pos, world.getBlockState(pos), placeStateID, pkt.side);

                        final TileEntity te = world.getTileEntity(pos);
                        if (te instanceof ChiseledBlockTileEntity) {
                            final ChiseledBlockTileEntity tec = (ChiseledBlockTileEntity) te;
                            final VoxelBlob vb = tec.getBlob();
                            final ChiselIterator i = getIterator(pkt, new VoxelRegionSrc(world, pos, 1), pos, pkt.operation);
                            final Map<Integer, Long> extracted = new HashMap<>();

                            //Determine the capacity = the durability we have to use
                            int durabilityTaken = 0;
                            int totalCapacity = 0;

                            //In creative we don't care about the capacity.
                            if (isCreative) totalCapacity = Integer.MAX_VALUE;
                            else {
                                //Determine the maximum durability that the player has.
                                for (ItemStack item : player.inventory.mainInventory) {
                                    if (item.getItem() instanceof ChiselItem)
                                        totalCapacity += (chisel.getMaxDamage() - chisel.getDamage());
                                }
                            }

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
                                        if (pkt.operation == SWAP && blk == placeStateID)
                                            continue;

                                        //Track how many bits we've extracted and it's not a coloured bit.
                                        if (pkt.operation == SWAP && !VoxelType.isColoured(blk))
                                            extracted.put(blk, extracted.getOrDefault(blk, 0L) + 1);

                                        vb.set(i.x(), i.y(), i.z(), placeStateID);
                                        //Test durability
                                        durabilityTaken += 1;
                                        if (durabilityTaken >= totalCapacity)
                                            break;

                                        //Test resources
                                        bitsUsed += 1;
                                        if (bitsUsed >= bitsAvailable)
                                            break;
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

                                        //Track how many bits we've extracted and it's not a coloured bit.
                                        if (!VoxelType.isColoured(blk))
                                            extracted.put(blk, extracted.getOrDefault(blk, 0L) + 1);

                                        vb.clear(i.x(), i.y(), i.z());
                                        durabilityTaken++;
                                        if (durabilityTaken >= totalCapacity)
                                            break;
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

                                //If there's still toGive left but nowhere to put it's voided...
                                //TODO Disable/Abort chiseling if you're not able to store all bits.
                            }

                            //We can use the durability taken to see if something happened because any operation will influence durability.
                            if (durabilityTaken > 0) {
                                //Actually apply the operation.
                                tec.completeEditOperation(vb);

                                //Send the breaking sound only if the block actually got changed.
                                SoundType st = world.getBlockState(pos).getSoundType();
                                world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, st.getBreakSound(), SoundCategory.BLOCKS, (st.getVolume() + 1.0F) / 16.0F, st.getPitch() * 0.9F);

                                //Below follows some unnecessarily complex code to lower the durability of the chisel and to automatically move backup chisels to the slot to continue chiseling when the old one breaks.
                                if (!isCreative) { //No tool damage in creative mode.
                                    //Swap chisel to a chisel item if it's currently not
                                    if(!(chisel.getItem() instanceof ChiselItem)) {
                                        for (int s = 0; s < player.inventory.mainInventory.size(); s++) {
                                            ItemStack item = player.inventory.mainInventory.get(s);
                                            if(item.getItem() instanceof ChiselItem) {
                                                chisel = item;
                                                break;
                                            }
                                        }

                                        if(!(chisel.getItem() instanceof ChiselItem))
                                            throw new IllegalArgumentException("Expected valid chisel but found none");
                                    }

                                    int usesLeft = chisel.getMaxDamage() - chisel.getDamage();
                                    //While there is durability to be taken we'll keep damaging tools.
                                    usesLeft -= durabilityTaken;
                                    while (durabilityTaken > 0) {
                                        //Remember the mode of the current chisel.
                                        IItemMode mode = ChiselModeManager.getMode(chisel);

                                        //We'll break the chisel as much as possible to the max of its durability.
                                        int capacity = Math.min(chisel.getMaxDamage() - chisel.getDamage(), durabilityTaken);
                                        chisel.damageItem(capacity, player, (p) -> {
                                            p.sendBreakAnimation(Hand.MAIN_HAND);
                                        });
                                        //Decrease the owed durability with as much as we've removed.
                                        durabilityTaken -= capacity;

                                        //Move new chisel to the front if the previous one broke so operations don't get halted halfway unnecessarily.
                                        boolean done = false;
                                        //Give a new one if the old chisel broke. (which is always the case if usesLeft < 0)
                                        if (usesLeft < 0 || chisel.getDamage() == chisel.getMaxDamage()) {
                                            for (int s = 0; s < player.inventory.mainInventory.size(); s++) {
                                                ItemStack item = player.inventory.mainInventory.get(s);
                                                if (item.getItem() instanceof ChiselItem) {
                                                    player.inventory.removeStackFromSlot(s);
                                                    //The previous item broke so the slot is always free.
                                                    player.inventory.mainInventory.set(player.inventory.currentItem, item);
                                                    ChiselModeManager.setMode(item, mode); //Keep the mode from your old chisel. Just a little quality of life!
                                                    chisel = item;
                                                    done = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (usesLeft < 0) { //If the current chisel item didn't have enough to last the operation we need to update our variables for the next round of the while loop.
                                            //If done failed we should have aborted the operation at some point, so this is a really bad scenario.
                                            if (!done)
                                                throw new IllegalArgumentException("Expected valid chisel but found none");

                                            usesLeft = chisel.getMaxDamage() - chisel.getDamage();
                                            usesLeft -= durabilityTaken;
                                        }
                                    }

                                    //Just for security, even though this should never happen.
                                    if (durabilityTaken > 0)
                                        throw new RuntimeException("Player didn't pay off durability debt, how did this happen?");
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            //TODO UndoTracker.getInstance().endGroup( who );
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
     * Get the currently selected bit type.
     */
    public static int getSelectedBit(final PlayerEntity player, final VoxelType type) {
        //TODO cache this value so we can query it really often from the morphing bit
        int ret = VoxelBlob.AIR_BIT;
        long stamp = 0;

        //Scan all storage containers for the most recently selected one.
        for (ItemStack item : player.inventory.mainInventory) {
            if (item.getItem() instanceof StorageItem) {
                if (type != null) {
                    //Cancel if we don't want this type.
                    if (type == VoxelType.BLOCKSTATE && !(item.getItem() instanceof BitBagItem)) continue;
                    if (type == VoxelType.FLUIDSTATE && !(item.getItem() instanceof BitBeakerItem)) continue;
                    if (type == VoxelType.COLOURED && !(item.getItem() instanceof PaletteItem)) continue;
                }

                long l = ChiselModeManager.getSelectionTime(item);
                if (l > stamp) {
                    stamp = l;
                    int ne = Optional.ofNullable(ChiselModeManager.getSelectedItem(item)).map(SelectedItemMode::getBitId).orElse(VoxelBlob.AIR_BIT);
                    if (ne != VoxelBlob.AIR_BIT)
                        ret = ne;
                }
            }
        }
        return ret;
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
        IFluidState fluid = world.getFluidState(pos);
        boolean isAir = isBlockReplaceable(player, world, pos, face, true);

        if (ChiselUtil.canChiselBlock(originalState) || isAir) {
            int blockId = isAir ? fragmentBlockStateID : ModUtil.getStateId(originalState);

            if (!target.equals(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK)) {
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
