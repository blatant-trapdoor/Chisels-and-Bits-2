package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;

public class BlockColorChiseled implements IBlockColor {
    @Override
    public int getColor(BlockState state, @Nullable IEnviromentBlockReader world, @Nullable BlockPos pos, int tint) {
        final TileEntity te = world != null ? world.getTileEntity(pos) : null;
        final BlockState tstate = te instanceof ChiseledBlockTileEntity ? ModUtil.getBlockState(((ChiseledBlockTileEntity) te).getPrimaryBlock()) : null;
        return Minecraft.getInstance().getBlockColors().getColor(tstate, world, pos, tint);
    }
}
