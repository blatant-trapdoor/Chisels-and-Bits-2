package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import nl.dgoossens.chiselsandbits2.common.utils.BitUtil;

import javax.annotation.Nullable;

public class ChiseledBlockColor extends ChiseledTintColor implements IBlockColor {
    private IEnviromentBlockReader world;
    private BlockPos pos;

    @Override
    public int getColor(BlockState state, @Nullable IEnviromentBlockReader world, @Nullable BlockPos pos, int tint) {
        this.world = world;
        this.pos = pos;
        return getColor(tint);
    }

    @Override
    protected int getDefaultColor(int v, int tintValue) {
        final BlockState tstate = BitUtil.getBlockState(v);
        return Minecraft.getInstance().getBlockColors().getColor(tstate, world, pos, tintValue);
    }
}
