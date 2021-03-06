package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import nl.dgoossens.chiselsandbits2.api.bit.BitStorage;
import nl.dgoossens.chiselsandbits2.api.bit.VoxelType;
import nl.dgoossens.chiselsandbits2.api.bit.VoxelWrapper;

import javax.annotation.Nullable;

public class StorageCapability implements Capability.IStorage<BitStorage> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<BitStorage> capability, BitStorage instance, Direction side) {
        if (instance instanceof BitStorageImpl)
            return instance.toNBT();
        return new CompoundNBT();
    }

    @Override
    public void readNBT(Capability<BitStorage> capability, BitStorage instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT)
            instance.loadFromNBT(nbt);
    }
}
