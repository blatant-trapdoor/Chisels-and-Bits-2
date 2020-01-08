package nl.dgoossens.chiselsandbits2.client.render.color;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import nl.dgoossens.chiselsandbits2.common.util.BitUtil;

import javax.annotation.Nullable;

public class ChiseledBlockColor extends ChiseledTintColor implements IBlockColor {
    private IEnviromentBlockReader world;
    private BlockPos pos;

    public ChiseledBlockColor() {}
    public ChiseledBlockColor(IEnviromentBlockReader world, BlockPos pos) {
        this.world = world;
        this.pos = pos;
    }

    @Override
    public int getColor(BlockState state, @Nullable IEnviromentBlockReader world, @Nullable BlockPos pos, int tint) {
        if(world != null) this.world = world;
        if(pos != null) this.pos = pos;
        return getColor(tint);
    }

    @Override
    public int getDefaultColor(int v, int t) {
        return Minecraft.getInstance().getBlockColors().getColor(BitUtil.getBlockState(v), world, pos, t);
    }
}
