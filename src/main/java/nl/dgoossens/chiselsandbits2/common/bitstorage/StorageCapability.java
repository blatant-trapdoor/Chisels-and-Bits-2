package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.api.BitStorage;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.api.VoxelWrapper;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;

public class StorageCapability implements Capability.IStorage<BitStorage> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<BitStorage> capability, BitStorage instance, Direction side) {
        CompoundNBT compound = new CompoundNBT();
        if (instance instanceof BitStorageImpl) {
            CompoundNBT content = new CompoundNBT();
            instance.list().forEach(k -> content.putLong(String.valueOf(k.getId()), instance.get(k)));
            compound.put("content", content);
            compound.put("type", new IntNBT(instance.getType().ordinal()));
        }
        return compound;
    }

    @Override
    public void readNBT(Capability<BitStorage> capability, BitStorage instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            CompoundNBT content = ((CompoundNBT) nbt).getCompound("content");
            content.keySet().forEach(k -> instance.set(VoxelWrapper.forAbstract(Integer.valueOf(k)), content.getLong(k)));
            instance.setType(VoxelType.values()[((CompoundNBT) nbt).getInt("type")]);
        }
    }
}
