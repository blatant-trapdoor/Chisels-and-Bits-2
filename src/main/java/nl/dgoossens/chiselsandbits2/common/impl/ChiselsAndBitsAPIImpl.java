package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.BitAccess;
import nl.dgoossens.chiselsandbits2.api.ChiselsAndBitsAPI;
import nl.dgoossens.chiselsandbits2.api.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.ItemModeEnum;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ChiselsAndBitsAPIImpl implements ChiselsAndBitsAPI {
    private Set<ItemModeEnum> itemModes = new HashSet<>();
    private Set<IItemModeType> itemModeTypes = new HashSet<>();

    @Override
    public void registerItemMode(ItemModeEnum itemMode) {
        itemModes.add(itemMode);
    }

    @Override
    public void registerItemModeType(IItemModeType itemModeType) {
        itemModeTypes.add(itemModeType);
    }

    @Override
    public Set<IItemModeType> getItemModeTypes() {
        return itemModeTypes;
    }

    @Override
    public Set<ItemModeEnum> getItemModes() {
        return itemModes;
    }

    @Override
    public Optional<BitAccess> getBitAccess(World world, BlockPos pos) {
        BitAccess ba = new BitAccessImpl(world, pos);
        if (ba.getNativeBlob() == null) return Optional.empty();
        return Optional.of(ba);
    }
}
