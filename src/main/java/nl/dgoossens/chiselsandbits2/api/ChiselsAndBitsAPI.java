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
     * Get the
     */
    Optional<BitAccess> getBitAccess(final World world, final BlockPos pos);

    /**
     * Get the default block slot used by all chisels & bit blocks.
     */
    //IBlockSlot getChiselsAndBitsSlot(); //TODO when the multipart API drops all C&B blocks should be put in their own slot
}
