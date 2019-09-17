package nl.dgoossens.chiselsandbits2.common.network.packets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
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

    private PacketChisel() {
    }

    public PacketChisel(
            final BitOperation operation,
            final BitLocation from,
            final BitLocation to,
            final Direction side,
            final IItemMode mode) {
        this.operation = operation;
        this.from = BitLocation.min(from, to);
        this.to = BitLocation.max(from, to);
        this.side = side;
        this.mode = mode;
    }

    public PacketChisel(
            final BitOperation operation,
            final BitLocation location,
            final Direction side,
            final IItemMode mode) {
        this.operation = operation;
        from = to = location;
        this.side = side;
        this.mode = mode;
    }

    public static ReplaceWithChiseledValue replaceWithChiseled(final @Nonnull PlayerEntity player, final @Nonnull World world, final @Nonnull BlockPos pos, final BlockState originalState, final int fragmentBlockStateID) {
        Block target = originalState.getBlock();
        final boolean isAir = world.isAirBlock(pos);
        ReplaceWithChiseledValue rv = new ReplaceWithChiseledValue();

        if (ChiselUtil.canChiselBlock(originalState) || isAir) {
            int blockId = isAir ? fragmentBlockStateID : ModUtil.getStateId(originalState);

            if (!target.equals(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK)) {
                world.setBlockState(pos, ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK.getDefaultState(), 3);
                final ChiseledBlockTileEntity te = (ChiseledBlockTileEntity) world.getTileEntity(pos);
                if (te != null) {
                    if (!isAir) te.fillWith(blockId);
                    rv.success = true;
                    rv.te = te;
                    return rv;
                }
            }
        }
        return rv;
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

        boolean update = false;

        //TODO UndoTracker.getInstance().beginGroup( who );

        try {
            for (int xOff = minX; xOff <= maxX; ++xOff) {
                for (int yOff = minY; yOff <= maxY; ++yOff) {
                    for (int zOff = minZ; zOff <= maxZ; ++zOff) {
                        final BlockPos pos = new BlockPos(xOff, yOff, zOff);

                        BlockState blkstate = world.getBlockState(pos);
                        Block blkObj = blkstate.getBlock();

                        final int placeStateID = ModUtil.getColourId(Color.GREEN);
                        /*operation.usesBits() ? ItemChiseledBit.getStackState( who.getHeldItem( hand ) ) : 0;
						final IContinuousInventory chisels = new ContinousChisels( player, pos, side );
						final IContinuousInventory bits = new ContinousBits( player, pos, placeStateID );



						if ( operation.usesChisels() )
						{
							if ( !chisels.isValid() || blkObj == null || blkstate == null || !ItemChisel.canMine( chisels, blkstate, who, world, pos ) )
							{
								continue;
							}
						}

						if ( operation.usesBits() )
						{
							if ( !bits.isValid() || blkObj == null || blkstate == null )
							{
								continue;
							}
						}*/

                        if (world.getServer() != null && world.getServer().isBlockProtected(world, pos, player))
                            continue;

                        //if ( world.getBlockState( pos ).getBlock().isReplaceable( world, pos ) )
                        //{
                        //world.removeBlock( pos, false );
                        //}

                        ReplaceWithChiseledValue rv = replaceWithChiseled(player, world, pos, blkstate, placeStateID);
                        if (rv.success) {
                            blkstate = world.getBlockState(pos);
                            blkObj = blkstate.getBlock();
                        }

                        final TileEntity te = rv.te != null ? rv.te : world.getTileEntity(pos);
                        if (te instanceof ChiseledBlockTileEntity) {
                            final ChiseledBlockTileEntity tec = (ChiseledBlockTileEntity) te;
                            final VoxelBlob vb = tec.getBlob();
                            final boolean isCreative = player.isCreative();

                            final ChiselIterator i = getIterator(new VoxelRegionSrc(world, pos, 1), pos, operation);
                            switch (operation) {
                                case PLACE:
                                case REPLACE: {
                                    while (i.hasNext()) {
                                        if (operation == BitOperation.REPLACE || vb.get(i.x(), i.y(), i.z()) == VoxelBlob.AIR_BIT) {
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
                                            //}
                                            update = true;
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
                                        update = true;
                                    }
                                }
                                break;
                            }

                            if (update)
                                tec.completeEditOperation(vb);
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
        //TODO update chisel durability (server-side)
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

    public static class ReplaceWithChiseledValue {
        public boolean success = false;
        public ChiseledBlockTileEntity te = null;
    }

    public static class Handler {
        public static void handle(final PacketChisel pkt, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> pkt.doAction(ctx.get().getSender()));
        }
    }
}
