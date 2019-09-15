package nl.dgoossens.chiselsandbits2.network.packets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.IVoxelSrc;
import nl.dgoossens.chiselsandbits2.api.BitOperation;
import nl.dgoossens.chiselsandbits2.api.ItemMode;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.iterators.ChiselTypeIterator;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelRegionSrc;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;
import nl.dgoossens.chiselsandbits2.network.NetworkRouter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class PacketChisel implements NetworkRouter.ModPacket {
	private static final float one_16th = 1.0f / 16.0f;

	private BitLocation from;
	private BitLocation to;

	private BitOperation operation;
	private Direction side;
	private IItemMode mode;

	private PacketChisel() {}
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

	public int doAction(final PlayerEntity player) {
		final World world = player.world;

		final int minX = Math.min( from.blockPos.getX(), to.blockPos.getX() );
		final int maxX = Math.max( from.blockPos.getX(), to.blockPos.getX() );
		final int minY = Math.min( from.blockPos.getY(), to.blockPos.getY() );
		final int maxY = Math.max( from.blockPos.getY(), to.blockPos.getY() );
		final int minZ = Math.min( from.blockPos.getZ(), to.blockPos.getZ() );
		final int maxZ = Math.max( from.blockPos.getZ(), to.blockPos.getZ() );

		int returnVal = 0;

		boolean update = false;
		Map<Integer, Integer> extracted = new HashMap<>();
		ItemStack bitPlaced = null;

		final List<ItemEntity> spawnlist = new ArrayList<>();

		//TODO UndoTracker.getInstance().beginGroup( who );

		try
		{
			for ( int xOff = minX; xOff <= maxX; ++xOff )
			{
				for ( int yOff = minY; yOff <= maxY; ++yOff )
				{
					for ( int zOff = minZ; zOff <= maxZ; ++zOff )
					{
						final BlockPos pos = new BlockPos( xOff, yOff, zOff );

						BlockState blkstate = world.getBlockState( pos );
						Block blkObj = blkstate.getBlock();

						final int placeStateID = ModUtil.getStateId(Blocks.STONE.getDefaultState());/*operation.usesBits() ? ItemChiseledBit.getStackState( who.getHeldItem( hand ) ) : 0;
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

						if(world.getServer() != null && world.getServer().isBlockProtected(world, pos, player)) continue;

						//if ( world.getBlockState( pos ).getBlock().isReplaceable( world, pos ) )
						//{
							//world.removeBlock( pos, false );
						//}

						ReplaceWithChiseledValue rv = null;
						if((rv = replaceWithChiseled(world, pos, blkstate, placeStateID, true)).success) {
							blkstate = world.getBlockState(pos);
							blkObj = blkstate.getBlock();
						}

						final TileEntity te = rv.te != null ? rv.te : world.getTileEntity(pos);

						if (te instanceof ChiseledBlockTileEntity) {
							final ChiseledBlockTileEntity tec = (ChiseledBlockTileEntity) te;
							final VoxelBlob vb = tec.getBlob();
							final boolean isCreative = player.isCreative();

							final ChiselIterator i = getIterator(new VoxelRegionSrc( world, pos, 1), pos, operation);
							switch(operation) {
								case PLACE:
								case REPLACE:
								{
									while(i.hasNext()) {
										bitPlaced = new ItemStack(Blocks.STONE);//bits.getItem( 0 ).getStack();
										if ( vb.get( i.x(), i.y(), i.z() ) == 0 )
										{
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
											vb.set( i.x(), i.y(), i.z(), placeStateID );
											//}

											update = true;
										}
									}
								}
								break;
								case REMOVE:
								{
									while(i.hasNext()) {
										final int blk = vb.get( i.x(), i.y(), i.z() );
										if ( blk == 0 ) break;
										//TODO Chisel valid? if ( !selected.useItem( blk ) ) break;

										if ( !world.isRemote && !isCreative )
										{
											double hitX = i.x() * one_16th;
											double hitY = i.y() * one_16th;
											double hitZ = i.z() * one_16th;

											final double offset = 0.5;
											hitX += side.getXOffset() * offset;
											hitY += side.getYOffset() * offset;
											hitZ += side.getZOffset() * offset;

											//TODO return bit
											/*if ( extracted == null || !ItemChiseledBit.sameBit( extracted, blk ) || ModUtil.getStackSize( extracted ) == 64 )
											{
												extracted = ItemChiseledBit.createStack( blk, 1, true );
												spawnlist.add( new EntityItem( world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, extracted ) );
											}
											else
											{
												ModUtil.adjustStackSize( extracted, 1 );
											}*/
										}
										else
										{
											// return value...
											//extracted = ItemChiseledBit.createStack( blk, 1, true );
										}

										int current = vb.get(i.x(), i.y(), i.z());
										extracted.put(current, extracted.getOrDefault(current, 0)+1);
										vb.clear(i.x(), i.y(), i.z());
									}
								}
								break;
							}


							if ( update )
							{
								tec.completeEditOperation( vb );
								returnVal++;
							}
							else if ( !extracted.isEmpty() )
							{
								tec.completeEditOperation( vb );
								returnVal++;
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

		}
		finally
		{
			//TODO UndoTracker.getInstance().endGroup( who );
		}
		//TODO update chisel durability (server-side)

		return returnVal;
	}

	public static class ReplaceWithChiseledValue {
		public boolean success = false;
		public ChiseledBlockTileEntity te = null;
	}

	public static ReplaceWithChiseledValue replaceWithChiseled(
			final @Nonnull World world,
			final @Nonnull BlockPos pos,
			final BlockState originalState,
			final int fragmentBlockStateID,
			final boolean triggerUpdate )
	{
		BlockState actingState = originalState;
		Block target = originalState.getBlock();
		final boolean isAir = world.isAirBlock(pos); //TODO add isReplacable for grass or smth
		ReplaceWithChiseledValue rv = new ReplaceWithChiseledValue();

		if(ChiselUtil.canChiselBlock(actingState) || isAir) {
			int blockId = ModUtil.getStateId(actingState);

			if(isAir) {
				actingState = ModUtil.getBlockState( fragmentBlockStateID );
				target = actingState.getBlock();
				blockId = ModUtil.getStateId( actingState );
				// its still air tho..
				actingState = Blocks.AIR.getDefaultState();
			}

			if(blockId == 0) return rv;

			if (!target.equals(ChiselsAndBits2.getBlocks().CHISELED_BLOCK)) {
				world.setBlockState(pos, ChiselsAndBits2.getBlocks().CHISELED_BLOCK.getDefaultState(), triggerUpdate ? 3 : 0);
				final TileEntity te = world.getTileEntity(pos);

				ChiseledBlockTileEntity tec;
				if (!(te instanceof ChiseledBlockTileEntity)) {
					tec = (ChiseledBlockTileEntity) ChiselsAndBits2.getBlocks().CHISELED_BLOCK.createTileEntity(ChiselsAndBits2.getBlocks().CHISELED_BLOCK.getDefaultState(), world);
					world.setTileEntity( pos, tec );
				} else tec = (ChiseledBlockTileEntity) te;

				if(tec != null) tec.fillWith(actingState);

				rv.success = true;
				rv.te = tec;

				return rv;
			}
		}

		return rv;
	}

	private ChiselIterator getIterator(
			final IVoxelSrc vb,
			final BlockPos pos,
			final BitOperation place ) {
		if(mode == ItemMode.CHISEL_DRAWN_REGION) {
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

	private static BitLocation readBitLoc(final PacketBuffer buffer) {
		return new BitLocation( buffer.readBlockPos(), buffer.readByte(), buffer.readByte(), buffer.readByte() );
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
			pc.mode = ChiselModeManager.resolveMode(buffer.readString());
		} catch(Exception x) { x.printStackTrace(); }
		return pc;
	}

	public static class Handler {
		public static void handle(final PacketChisel pkt, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() ->
				pkt.doAction(ctx.get().getSender())
			);
		}
	}
}
