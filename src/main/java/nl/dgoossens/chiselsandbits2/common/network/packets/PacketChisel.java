package nl.dgoossens.chiselsandbits2.common.network.packets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitOperation;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.IVoxelSrc;
import nl.dgoossens.chiselsandbits2.api.ItemMode;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselTypeIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelRegionSrc;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketChisel implements NetworkRouter.ModPacket {
    private static final float one_16th = 1.0f / 16.0f;

    private BitLocation from;
    private BitLocation to;
    private BitOperation operation;
    private Direction side;
    private IItemMode mode;

    private PacketChisel() { }
    public PacketChisel(final BitOperation operation, final BitLocation from, final BitLocation to, final Direction side, final IItemMode mode) {
        this.operation = operation;
        this.from = BitLocation.min(from, to);
        this.to = BitLocation.max(from, to);
        this.side = side;
        this.mode = mode;
    }

    public PacketChisel(final BitOperation operation, final BitLocation location, final Direction side, final IItemMode mode) {
        this.operation = operation;
        from = to = location;
        this.side = side;
        this.mode = mode;
    }

    public static void replaceWithChiseled(final @Nonnull PlayerEntity player, final @Nonnull World world, final @Nonnull BlockPos pos, final BlockState originalState, final int fragmentBlockStateID, final Direction face) {
        Block target = originalState.getBlock();
        boolean isAir = world.isAirBlock(pos);

        //We see it as air if we can replace it.
        System.out.println("Pos at "+pos+", but can we replace it?");
        if(!isAir && world.getBlockState(pos).isReplaceable(new BlockItemUseContext(new ItemUseContext(player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d((double)pos.getX() + 0.5D + (double)face.getXOffset() * 0.5D, (double)pos.getY() + 0.5D + (double)face.getYOffset() * 0.5D, (double)pos.getZ() + 0.5D + (double)face.getZOffset() * 0.5D), face, pos, false))))) {
            isAir = true;
            System.out.println("Pos at "+pos+" is indeed replaceable");
        }

        if (ChiselUtil.canChiselBlock(originalState) || isAir) {
            int blockId = isAir ? fragmentBlockStateID : ModUtil.getStateId(originalState);

            if (!target.equals(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK)) {
                world.setBlockState(pos, ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK.getDefaultState(), 3);
                final ChiseledBlockTileEntity te = (ChiseledBlockTileEntity) world.getTileEntity(pos);
                if (te != null) {
                    if (!isAir) te.fillWith(blockId);
                    else te.fillWith(VoxelBlob.AIR_BIT);
                }
            }
        }
    }

    private static BitLocation readBitLoc(final PacketBuffer buffer) {
        return new BitLocation(buffer.readBlockPos(), buffer.readByte(), buffer.readByte(), buffer.readByte());
    }

    private static void writeBitLoc(final BitLocation from2, final PacketBuffer buffer) {
        buffer.writeBlockPos(from2.blockPos);
        buffer.writeByte(from2.bitX);
        buffer.writeByte(from2.bitY);
        buffer.writeByte(from2.bitZ);
    }

    public static void encode(PacketChisel msg, PacketBuffer buf) {
        writeBitLoc(msg.from, buf);
        writeBitLoc(msg.to, buf);

        buf.writeEnumValue(msg.operation);
        buf.writeVarInt(msg.side.ordinal());
        buf.writeString(msg.mode.getName());
    }

    public static PacketChisel decode(PacketBuffer buffer) {
        PacketChisel pc = new PacketChisel();
        pc.from = readBitLoc(buffer);
        pc.to = readBitLoc(buffer);

        pc.operation = buffer.readEnumValue(BitOperation.class);
        pc.side = Direction.values()[buffer.readVarInt()];
        try {
            pc.mode = ChiselModeManager.resolveMode(buffer.readString(), null);
        } catch (Exception x) {
            x.printStackTrace();
        }
        return pc;
    }

    public void doAction(final PlayerEntity player) {
        final World world = player.world;

        final int minX = Math.min(from.blockPos.getX(), to.blockPos.getX());
        final int maxX = Math.max(from.blockPos.getX(), to.blockPos.getX());
        final int minY = Math.min(from.blockPos.getY(), to.blockPos.getY());
        final int maxY = Math.max(from.blockPos.getY(), to.blockPos.getY());
        final int minZ = Math.min(from.blockPos.getZ(), to.blockPos.getZ());
        final int maxZ = Math.max(from.blockPos.getZ(), to.blockPos.getZ());

        //TODO UndoTracker.getInstance().beginGroup( who );

        try {
            for (int xOff = minX; xOff <= maxX; ++xOff) {
                for (int yOff = minY; yOff <= maxY; ++yOff) {
                    for (int zOff = minZ; zOff <= maxZ; ++zOff) {
                        final BlockPos pos = new BlockPos(xOff, yOff, zOff);

                        BlockState blkstate = world.getBlockState(pos);
                        ItemStack chisel = player.getHeldItemMainhand();
                        if(!(chisel.getItem() instanceof ChiselItem))
                            return; //Extra security, if you're somehow no longer holding a chisel we cancel.

                        final int placeStateID = ModUtil.getStateId(Blocks.DIAMOND_BLOCK.getDefaultState());

                        if (world.getServer() != null && world.getServer().isBlockProtected(world, pos, player))
                            continue;

                        replaceWithChiseled(player, world, pos, blkstate, placeStateID, side);
                        final TileEntity te = world.getTileEntity(pos);
                        if (te instanceof ChiseledBlockTileEntity) {
                            final ChiseledBlockTileEntity tec = (ChiseledBlockTileEntity) te;
                            final VoxelBlob vb = tec.getBlob();
                            final boolean isCreative = player.isCreative();

                            final ChiselIterator i = getIterator(new VoxelRegionSrc(world, pos, 1), pos, operation);
                            int durabilityTaken = 0;
                            int totalCapacity = 0;
                            //Determine the maximum durability that the player has.
                            for(ItemStack item : player.inventory.mainInventory) {
                                if(item.getItem() instanceof ChiselItem)
                                    totalCapacity += (chisel.getMaxDamage() - chisel.getDamage());
                            }

                            switch (operation) {
                                case PLACE: {
                                    while (i.hasNext()) {
                                        if (vb.get(i.x(), i.y(), i.z()) == VoxelBlob.AIR_BIT) {
                                            //final IItemInInventory slot = bits.getItem( 0 );
                                            //final int stateID = ItemChiseledBit.getStackState( slot.getStack() );

                                            //if ( slot.isValid() )
                                            //{
                                            //	if ( !player.isCreative() )
                                            //	{
                                            //		if ( bits.useItem( stateID ) )
                                            //			vb.set( x, y, z, stateID );
                                            //	}
                                            //	else
                                            vb.set(i.x(), i.y(), i.z(), placeStateID);
                                            durabilityTaken += 1;
                                            if(durabilityTaken >= totalCapacity) {
                                                break;
                                            }
                                            //}
                                        }
                                    }
                                }
                                break;
                                case REPLACE: {
                                    while (i.hasNext()) {
                                        vb.set(i.x(), i.y(), i.z(), placeStateID);
                                        durabilityTaken += 1;
                                        if(durabilityTaken >= totalCapacity) {
                                            break;
                                        }
                                    }
                                }
                                break;
                                case REMOVE: {
                                    while (i.hasNext()) {
                                        final int blk = vb.get(i.x(), i.y(), i.z());
                                        if (blk == VoxelBlob.AIR_BIT) break; //TODO this seems questionable
                                        //TODO Chisel valid? if ( !selected.useItem( blk ) ) break;

                                        /*if (!world.isRemote && !isCreative) {
                                            double hitX = i.x() * one_16th;
                                            double hitY = i.y() * one_16th;
                                            double hitZ = i.z() * one_16th;

                                            final double offset = 0.5;
                                            hitX += side.getXOffset() * offset;
                                            hitY += side.getYOffset() * offset;
                                            hitZ += side.getZOffset() * offset;

                                            //TODO return bit
											*if ( extracted == null || !ItemChiseledBit.sameBit( extracted, blk ) || ModUtil.getStackSize( extracted ) == 64 )
											{
												extracted = ItemChiseledBit.createStack( blk, 1, true );
												spawnlist.add( new EntityItem( world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, extracted ) );
											}
											else
											{
												ModUtil.adjustStackSize( extracted, 1 );
											}*
                                        } else {
                                            // return value...
                                            //extracted = ItemChiseledBit.createStack( blk, 1, true );
                                        }*/
                                        vb.clear(i.x(), i.y(), i.z());
                                        durabilityTaken++;
                                        if(durabilityTaken >= totalCapacity) {
                                            break;
                                        }
                                    }
                                }
                                break;
                            }

                            if (durabilityTaken > 0) {
                                tec.completeEditOperation(vb);

                                //Below follows some incredibly complex code to lower the durability of the chisel and to automatically move backup chisels to the slot to continue chiseling when the old one breaks.
                                if(!isCreative) { //No tool damage in creative mode.
                                    int usesLeft = chisel.getMaxDamage() - chisel.getDamage();
                                    //While there is durability to be taken we'll keep damaging tools.
                                    usesLeft -= durabilityTaken;
                                    while(durabilityTaken > 0) {
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
                                        if(usesLeft < 0 || chisel.getDamage() == chisel.getMaxDamage()) {
                                            for(int s = 0; s < player.inventory.mainInventory.size(); s++) {
                                                ItemStack item = player.inventory.mainInventory.get(s);
                                                if(item.getItem() instanceof ChiselItem) {
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

                                        if(usesLeft < 0) { //If the current chisel item didn't have enough to last the operation we need to update our variables for the next round of the while loop.
                                            //If done failed we should have aborted the operation at some point, so this is a really bad scenario.
                                            if(!done)
                                                throw new IllegalArgumentException("Expected valid chisel but found none");

                                            usesLeft = chisel.getMaxDamage() - chisel.getDamage();
                                            usesLeft -= durabilityTaken;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

			/*BitInventoryFeeder feeder = new BitInventoryFeeder( who, world );
			for ( final EntityItem ei : spawnlist )
			{
				feeder.addItem( ei );
				ItemBitBag.cleanupInventory( who, ei.getEntityItem() );
			}

			if ( operation.usesBits() )
			{
				ItemBitBag.cleanupInventory( who, bitPlaced != null ? bitPlaced : new ItemStack( ChiselsAndBits.getItems().itemBlockBit, 1, OreDictionary.WILDCARD_VALUE ) );
			}*/

        } finally {
            //TODO UndoTracker.getInstance().endGroup( who );
        }
    }

    private ChiselIterator getIterator(final IVoxelSrc vb, final BlockPos pos, final BitOperation place) {
        if (mode == ItemMode.CHISEL_DRAWN_REGION) {
            final int bitX = pos.getX() == from.blockPos.getX() ? from.bitX : 0;
            final int bitY = pos.getY() == from.blockPos.getY() ? from.bitY : 0;
            final int bitZ = pos.getZ() == from.blockPos.getZ() ? from.bitZ : 0;

            final int scaleX = (pos.getX() == to.blockPos.getX() ? to.bitX : 15) - bitX + 1;
            final int scaleY = (pos.getY() == to.blockPos.getY() ? to.bitY : 15) - bitY + 1;
            final int scaleZ = (pos.getZ() == to.blockPos.getZ() ? to.bitZ : 15) - bitZ + 1;

            return new ChiselTypeIterator(VoxelBlob.DIMENSION, bitX, bitY, bitZ, scaleX, scaleY, scaleZ, side);
        }
        return ChiselTypeIterator.create(VoxelBlob.DIMENSION, from.bitX, from.bitY, from.bitZ, vb, mode, side, place.equals(BitOperation.PLACE));
    }

    public static class Handler {
        public static void handle(final PacketChisel pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> pkt.doAction(ctx.get().getSender()));
        }
    }
}
