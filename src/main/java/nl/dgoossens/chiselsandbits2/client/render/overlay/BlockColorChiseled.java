package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nullable;

public class BlockColorChiseled implements IBlockColor {
    private static final int TINT_MASK = 0xff;
    private static final int TINT_BITS = 8;

    @Override
    public int getColor(BlockState state, @Nullable IEnviromentBlockReader world, @Nullable BlockPos pos, int tint) {
        final BlockState tstate = ModUtil.getStateById(tint >> TINT_BITS);
        int tintValue = tint & TINT_MASK;
        return Minecraft.getInstance().getBlockColors().getColor(tstate, world, pos, tintValue);
    }
}
