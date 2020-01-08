package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayerFactory;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.block.BitAccess;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.utils.BitUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;

import javax.annotation.Nullable;

public class BitAccessImpl implements BitAccess {
    private final World world;
    private final BlockPos pos;
    private VoxelBlob blob = null;

    public BitAccessImpl(final PlayerEntity player, final World world, final BlockPos pos) {
        this.world = world;
        this.pos = pos;

        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ChiseledBlockTileEntity)
            blob = ((ChiseledBlockTileEntity) te).getVoxelBlob();
        else {
            if (!ChiselUtil.isBlockReplaceable(player, world, pos, Direction.UP, false))
                return;
            blob = new VoxelBlob();
        }
    }

    @Nullable
    public VoxelBlob getNativeBlob() {
        return blob;
    }
}
