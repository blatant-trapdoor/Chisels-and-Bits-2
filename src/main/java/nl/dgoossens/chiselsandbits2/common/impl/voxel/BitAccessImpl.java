package nl.dgoossens.chiselsandbits2.common.impl.voxel;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.block.BitAccess;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.util.ChiselUtil;

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
            if (!ChiselUtil.isBlockReplaceable(world, pos, player, Direction.UP, false))
                return;
            blob = new VoxelBlob();
        }
    }

    @Nullable
    public VoxelBlob getNativeBlob() {
        return blob;
    }
}
