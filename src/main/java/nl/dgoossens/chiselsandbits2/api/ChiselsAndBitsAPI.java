package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.Set;

/**
 * The Chisels & Bits API is a way for any mod (including C&B2 itself)
 * to interact with C&B tiles and modify them.
 * Much of C&Bs functionality can also be found in the various utility classes
 * in the common.utils package.
 *
 * If you'd like more of C&B to be accessible through the API, like how the API allows adding custom item modes, you can
 * make an issue requesting it. No part of C&B should be hidden from other mods wanting to use it and most enums should
 * be extendable. However, something like VoxelType can't be extended and no PR trying to do so will be merged.
 */
public interface ChiselsAndBitsAPI {
    /**
     * Get an entry point to a given block position in a given world.
     * The BitAccess allows access and easy manipulation of the voxel data of a given
     * chiseled block.
     */
    Optional<BitAccess> getBitAccess(final World world, final BlockPos pos);

    /**
     * Registers a new value from an item mode enum. Can be used to add new modes to
     * item mode types.
     */
    void registerItemMode(final ItemModeEnum itemMode);

    /**
     * Register a new item mode type.
     */
    void registerItemModeType(final IItemModeType itemModeType);

    /**
     * Get a list of all existing item modes.
     */
    Set<ItemModeEnum> getItemModes();

    /**
     * Get a list of all existing item mode types.
     */
    Set<IItemModeType> getItemModeTypes();
}
