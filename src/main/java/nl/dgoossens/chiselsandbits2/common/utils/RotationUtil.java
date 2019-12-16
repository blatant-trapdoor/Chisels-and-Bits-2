package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.BlockState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;

import static net.minecraft.util.Direction.*;

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
            if (curr == axis) return inputState;
            //Get the axis that its not already on and that we are not rotating on.
            for(Direction.Axis ax : Direction.Axis.values()) {
                if(ax != curr && ax != axis)
                    return BitUtil.getBlockId(in.with(BlockStateProperties.AXIS, ax));
            }
        } else if(in.has(BlockStateProperties.FACING)) {
            final Direction curr = in.get(BlockStateProperties.FACING);
            return BitUtil.getBlockId(in.with(BlockStateProperties.FACING, spinFacing(curr, axis, backwards)));
        }

        //The following properties aren't spun as they can't be due to complications: ROTATION_0_15, HORIZONTAL_FACING, FACING_EXCEPT_UP
        return inputState;
    }

    private static Direction spinFacing(final Direction curr, final Axis axis, final boolean backwards) {
        return backwards ? rotateAroundCCW(curr, axis) : curr.rotateAround(axis);
    }

    //Interal method, opposite of Direction#rotateAround
    private static Direction rotateAroundCCW(final Direction curr, final Direction.Axis axis) {
        switch(axis) {
            case X:
                if (curr != WEST && curr != EAST) {
                    return rotateXCCW(curr);
                }

                return curr;
            case Y:
                if (curr != UP && curr != DOWN) {
                    return curr.rotateYCCW();
                }

                return curr;
            case Z:
                if (curr != NORTH && curr != SOUTH) {
                    return rotateZCCW(curr);
                }

                return curr;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    private static Direction rotateXCCW(final Direction curr) {
        switch(curr) {
            case NORTH:
                return UP;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + curr);
            case SOUTH:
                return DOWN;
            case UP:
                return SOUTH;
            case DOWN:
                return NORTH;
        }
    }

    private static Direction rotateZCCW(final Direction curr) {
        switch(curr) {
            case EAST:
                return UP;
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + curr);
            case WEST:
                return DOWN;
            case UP:
                return WEST;
            case DOWN:
                return EAST;
        }
    }
}
