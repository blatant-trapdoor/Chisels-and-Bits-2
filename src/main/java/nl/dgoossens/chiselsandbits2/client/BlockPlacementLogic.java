package nl.dgoossens.chiselsandbits2.client;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.BitLocation;
import nl.dgoossens.chiselsandbits2.common.impl.ItemMode;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;

public class BlockPlacementLogic {
    /**
     * Returns whether or not a block is placeable.
     */
    public static boolean isNormallyPlaceable(final PlayerEntity player, final World world, final BlockPos pos, final Direction face, final NBTBlobConverter nbt) {
        if(ChiselUtil.isBlockReplaceable(player, world, pos, face, false))
            return true;

        if(ItemModeUtil.getChiseledBlockMode(player).equals(ItemMode.CHISELED_BLOCK_FIT)) {
            if(world.getTileEntity(pos) instanceof ChiseledBlockTileEntity) {
                ChiseledBlockTileEntity cbte = (ChiseledBlockTileEntity) world.getTileEntity(pos);
                if(cbte != null && !nbt.getVoxelBlob().canMerge(cbte.getBlob()))
                    return false; //Can't place if we can't merge this
                return true;
            }
        }
        switch((ItemMode) ItemModeUtil.getChiseledBlockMode(player)) {
            case CHISELED_BLOCK_GRID:
                return false;
            default:
                return world.getTileEntity(pos) instanceof ChiseledBlockTileEntity;
        }
    }

    /**
     * Returns whether or not a block is placeable when placing off-grid.
     */
    public static boolean isPlaceableOffgrid(final PlayerEntity player, final World world, final Direction face, final BitLocation bl) {
        return false;
    }
}
