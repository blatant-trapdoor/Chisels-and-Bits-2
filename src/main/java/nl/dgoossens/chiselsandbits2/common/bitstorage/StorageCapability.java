package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.api.BitStorage;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;

public class StorageCapability implements Capability.IStorage<BitStorage> {
    @Nullable
    @Override
    public INBT writeNBT(Capability<BitStorage> capability, BitStorage instance, Direction side) {
        CompoundNBT compound = new CompoundNBT();
        if (instance instanceof BitStorageImpl) {
            CompoundNBT blockContent = new CompoundNBT();
            ((BitStorageImpl) instance).blocks.forEach((k, v) ->
                    blockContent.putLong(k.getRegistryName().toString(), v));
            compound.put("blockContent", blockContent);
            CompoundNBT fluidContent = new CompoundNBT();
            ((BitStorageImpl) instance).fluids.forEach((k, v) ->
                    fluidContent.putLong(k.getRegistryName().toString(), v));
            compound.put("fluidContent", fluidContent);
            ArrayList<Integer> ints = new ArrayList<>();
            ((BitStorageImpl) instance).bookmarks.forEach(k -> ints.add(k.hashCode()));
            compound.put("bookmarks", new IntArrayNBT(ints));
        }
        return compound;
    }

    @Override
    public void readNBT(Capability<BitStorage> capability, BitStorage instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            CompoundNBT blockContent = ((CompoundNBT) nbt).getCompound("blockContent");
            CompoundNBT fluidContent = ((CompoundNBT) nbt).getCompound("fluidContent");
            int[] bookmarks = ((CompoundNBT) nbt).getIntArray("bookmarks");

            blockContent.keySet().forEach(k ->
                    instance.setAmount(ForgeRegistries.BLOCKS.getValue(ResourceLocation.create(k, ':')), blockContent.getLong(k)));
            fluidContent.keySet().forEach(k ->
                    instance.setAmount(ForgeRegistries.FLUIDS.getValue(ResourceLocation.create(k, ':')), fluidContent.getLong(k)));
            for (int i : bookmarks)
                instance.addBookmark(new Color(i, true));
        }
    }
}
