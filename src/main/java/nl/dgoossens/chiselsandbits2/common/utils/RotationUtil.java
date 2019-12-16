package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;

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
     * Get the mirroring to apply to all blockstates in a block when the block is mirrored around this axis.
     */
    public static Mirror getMirror(final Direction.Axis axis) {
        switch(axis) {
            case X: return Mirror.LEFT_RIGHT;
            case Y: return Mirror.NONE;
            case Z: return Mirror.FRONT_BACK;
        }
        return Mirror.NONE;
    }

    /**
     * Get the new block state that the original gets mapped to when rotating.
     */
    public static int spinBlockState(final int inputState, final Direction.Axis axis, final boolean backwards) {
        final BlockState in = BitUtil.getBlockState(inputState);
        if(!hasRotatableState(in)) return inputState;
        if(in.has(BlockStateProperties.AXIS)) {
            final Direction.Axis curr = in.get(BlockStateProperties.AXIS);
            //If axis remains the same, don't go to a different one.
            if (curr == axis) return BitUtil.getBlockId(in.with(BlockStateProperties.AXIS, curr));
            //Get the axis that its not already on and that we are not rotating on.
            for(Direction.Axis ax : Direction.Axis.values()) {
                if(ax != curr && ax != axis)
                    return BitUtil.getBlockId(in.with(BlockStateProperties.AXIS, ax));
            }
        }
        return inputState;
    }
}
