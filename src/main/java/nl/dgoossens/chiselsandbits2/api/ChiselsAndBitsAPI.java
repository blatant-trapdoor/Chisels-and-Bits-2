package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * The Chisels & Bits API is a way for any mod (including C&B itself)
 * to interact with C&B tiles and modify them.
 */
public interface ChiselsAndBitsAPI {
    /**
     * Get an entry point to a given block position in a given world.
     * The BitAccess allows access and easy manipulation of the voxel data of a given
     * chiseled block.
     */
    Optional<BitAccess> getBitAccess(final World world, final BlockPos pos);
}
