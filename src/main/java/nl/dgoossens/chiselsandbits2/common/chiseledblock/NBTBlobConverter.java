package nl.dgoossens.chiselsandbits2.common.chiseledblock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

public class NBTBlobConverter {
	public static final String NBT_PRIMARY_STATE = "b";
	public static final String NBT_VERSIONED_VOXEL = "X";

	private ChiseledBlockTileEntity tile;
	private int primaryBlockState;
	private VoxelBlobStateReference voxelBlobRef;
	private int format = -1;

	public int getPrimaryBlockStateID() { return primaryBlockState; }
	public ChiseledBlockTileEntity getTile() { return tile; }
	public BlockState getPrimaryBlockState() { return ModUtil.getBlockState(primaryBlockState); }

	public VoxelBlobStateReference getVoxelRef(final int version) throws Exception {
		final VoxelBlobStateReference voxelRef = getReference();
		if(format == version)
			return new VoxelBlobStateReference(voxelRef.getByteArray());
		return new VoxelBlobStateReference(voxelRef.getVoxelBlob().blobToBytes(version));
	}

	public NBTBlobConverter() {}
	public NBTBlobConverter(final ChiseledBlockTileEntity tile) {
		this.tile = tile;

		if(tile!=null) {
			primaryBlockState = tile.getPrimaryBlock();
			voxelBlobRef = tile.getVoxelReference();
			format = voxelBlobRef == null ? -1 : voxelBlobRef.getFormat();
		}
	}

	public void fillWith(final BlockState state) {
		voxelBlobRef = new VoxelBlobStateReference( ModUtil.getStateId( state ) );
		updatePrimaryStateFromBlob();
	}

	public void setBlob(final VoxelBlob vb) {
		voxelBlobRef = new VoxelBlobStateReference( vb );
		format = voxelBlobRef.getFormat();
		updatePrimaryStateFromBlob();
	}

	public final void writeChiselData(final CompoundNBT compound) {
		final VoxelBlobStateReference voxelRef = getReference();

		final int newFormat = VoxelVersions.getDefault();
		final byte[] voxelBytes = newFormat == format ? voxelRef.getByteArray() : voxelRef.getVoxelBlob().blobToBytes(newFormat);

		compound.putInt(NBT_PRIMARY_STATE, primaryBlockState);
		compound.putByteArray(NBT_VERSIONED_VOXEL, voxelBytes);
	}

	public final boolean readChiselData(final CompoundNBT compound, final int preferredFormat ) {
		if(compound == null || !compound.contains(NBT_PRIMARY_STATE) || !compound.contains(NBT_VERSIONED_VOXEL)) {
			voxelBlobRef = new VoxelBlobStateReference(VoxelBlob.AIR_BIT);
			format = voxelBlobRef.getFormat();

			if(tile != null)
				return tile.updateBlob(this);
			return false;
		}

		primaryBlockState = compound.getInt(NBT_PRIMARY_STATE);
		byte[] v = compound.getByteArray(NBT_VERSIONED_VOXEL);
		System.out.println("Read v of length "+v.length+" from compound "+compound);
		voxelBlobRef = new VoxelBlobStateReference(v);
		format = voxelBlobRef.getFormat();

		boolean formatChanged = false;
		if(preferredFormat != format && preferredFormat != VoxelVersions.ANY.getId()) {
			formatChanged = true;
			v = voxelBlobRef.getVoxelBlob().blobToBytes( preferredFormat );
			voxelBlobRef = new VoxelBlobStateReference(v);
			format = voxelBlobRef.getFormat();
		}

		if(tile != null) {
			if(formatChanged) {
				// this only works on already loaded tiles, so i'm not sure
				// there is much point in it.
				tile.markDirty();
			}

			return tile.updateBlob(this);
		}
		return true;
	}

	public void updatePrimaryStateFromBlob() {
		primaryBlockState = getReference().getVoxelBlob().getMostCommonStateId();
	}

	public ItemStack getItemStack() {
		final Block blk = ChiselsAndBits2.getBlocks().CHISELED_BLOCK;
		if(blk != null) {
			final ItemStack is = new ItemStack(blk);
			final CompoundNBT compound = is.getOrCreateChildTag(ModUtil.NBT_BLOCKENTITYTAG);
			writeChiselData(compound);
			if(!compound.isEmpty()) return is;
		}
		return null;
	}

	public VoxelBlobStateReference getReference() {
		if (voxelBlobRef == null) voxelBlobRef = new VoxelBlobStateReference(0);
		return voxelBlobRef;
	}

	public VoxelBlob getVoxelBlob() { return getReference().getVoxelBlob(); }
}
