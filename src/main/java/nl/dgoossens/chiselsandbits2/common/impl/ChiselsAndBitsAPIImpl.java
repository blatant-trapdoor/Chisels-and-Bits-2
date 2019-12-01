package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.block.BitAccess;
import nl.dgoossens.chiselsandbits2.api.ChiselsAndBitsAPI;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.item.ItemModeEnum;
import nl.dgoossens.chiselsandbits2.api.item.ItemPropertyAPI;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ChiselsAndBitsAPIImpl implements ChiselsAndBitsAPI {
    private ItemPropertyAPI itemPropertyAPI = new ItemPropertyAPIImpl();

    @Override
    public Optional<BitAccess> getBitAccess(World world, BlockPos pos) {
        BitAccess ba = new BitAccessImpl(world, pos);
        if (ba.getNativeBlob() == null) return Optional.empty();
        return Optional.of(ba);
    }

    @Override
    public ItemPropertyAPI getItemPropertyRegistry() {
        return itemPropertyAPI;
    }
}
