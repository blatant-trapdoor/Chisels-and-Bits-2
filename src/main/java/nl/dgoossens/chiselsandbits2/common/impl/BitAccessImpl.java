package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.BitAccess;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;

public class BitAccessImpl implements BitAccess {
    private final World world;
    private final BlockPos pos;
    private VoxelBlob blob = null;

    public BitAccessImpl(final World world, final BlockPos pos) {
        this.world=world; this.pos=pos;

        TileEntity te = world.getTileEntity(pos);
        if(te instanceof ChiseledBlockTileEntity)
            blob = ((ChiseledBlockTileEntity) te).getBlob();
        else {
            final BlockState state = world.getBlockState(pos);
            if(!ChiselUtil.canChiselBlock(state)) return;
            blob = new VoxelBlob();
            blob.fill(ModUtil.getStateId(state));
        }
    }

    @Nullable
    public VoxelBlob getNativeBlob() { return blob; }
}
