package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.BitAccess;
import nl.dgoossens.chiselsandbits2.api.ChiselsAndBitsAPI;

import java.util.Optional;

public class ChiselsAndBitsAPIImpl implements ChiselsAndBitsAPI {
    public Optional<BitAccess> getBitAccess(World world, BlockPos pos) {
        BitAccess ba = new BitAccessImpl(world, pos);
        if (ba.getNativeBlob() == null) return Optional.empty();
        return Optional.of(ba);
    }
}
