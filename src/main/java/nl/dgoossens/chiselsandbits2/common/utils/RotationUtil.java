package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;

import javax.annotation.Nullable;

/**
 * A utility handling rotation of minecraft blockstates. E.g. banners, signs, campfires, furnaces.
 */
public class RotationUtil {
    /**
     * Check if a block is rotatable using default minecraft rotation based block state properties.
     */
    public static boolean hasRotatableState(final BlockState state) {
        return state.getBlock() instanceof ChiseledBlock ||
                state.has(BlockStateProperties.AXIS) ||
                state.has(BlockStateProperties.HORIZONTAL_AXIS) ||
                state.has(BlockStateProperties.FACING) ||
                state.has(BlockStateProperties.FACING_EXCEPT_UP) ||
                state.has(BlockStateProperties.ROTATION_0_15) ||
                state.has(BlockStateProperties.HORIZONTAL_FACING);
    }

    /**
     * Check if a block is mirrorable using default minecraft rotation based block state properties.
     */
    public static boolean hasMirrorableState(final BlockState state) {
        return state.getBlock() instanceof ChiseledBlock ||
                state.has(BlockStateProperties.FACING) ||
                state.has(BlockStateProperties.FACING_EXCEPT_UP) ||
                state.has(BlockStateProperties.ROTATION_0_15) ||
                state.has(BlockStateProperties.HORIZONTAL_FACING);
    }

    /**
     * Get the new block state that the original gets mapped to when rotating around a given axis.
     * @param backwards True if counter clockwise.
     * @param mirror If not null, what type of mirroring to execute.
     */
    public static int mapBlockState(final int inputState, final boolean backwards, @Nullable final Mirror mirror) {
        final BlockState in = BitUtil.getBlockState(inputState);
        if(!hasRotatableState(in)) return inputState;
        BlockState out = mirror != null ? in.mirror(mirror) : in.rotate(backwards ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90);
        return BitUtil.getBlockId(out);
    }
}
