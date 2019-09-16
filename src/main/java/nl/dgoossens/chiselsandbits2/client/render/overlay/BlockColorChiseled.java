package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;

public class BlockColorChiseled implements IBlockColor {
    @Override
    public int getColor(BlockState state, @Nullable IEnviromentBlockReader world, @Nullable BlockPos pos, int tint) {
        final BlockState s = state.getBlock() instanceof ChiseledBlock ? ((ChiseledBlock) state.getBlock()).getPrimaryState(world, pos) : state;
        return s == null || s.getBlock() instanceof ChiseledBlock ? -1 : Minecraft.getInstance().getBlockColors().getColor(s, world, pos, tint);
    }
}
