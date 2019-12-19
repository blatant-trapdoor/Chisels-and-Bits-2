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
        CompoundNBT compound = new CompoundNBT();
        if (instance instanceof BitStorageImpl) {
            CompoundNBT content = new CompoundNBT();
            for(int i = 0; i < instance.getSlots(); i++) {
                CompoundNBT subcontent = new CompoundNBT();
                VoxelWrapper k = instance.getSlotContent(i);
                if(k == null) continue;
                subcontent.putLong(String.valueOf(k.getId()), instance.get(k));
                content.put(String.valueOf(i), subcontent);
            }
            compound.put("content", content);
            compound.put("type", new IntNBT(instance.getType().ordinal()));
        }
        return compound;
    }

    @Override
    public void readNBT(Capability<BitStorage> capability, BitStorage instance, Direction side, INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            CompoundNBT content = ((CompoundNBT) nbt).getCompound("content");
            for(String s : content.keySet()) {
                try {
                    int slot = Integer.valueOf(s);
                    CompoundNBT subcontent = content.getCompound(s);
                    String k = null;
                    for (String s2 : subcontent.keySet())
                        k = s2;
                    if (k == null) continue;
                    instance.setSlot(slot, VoxelWrapper.forAbstract(Integer.valueOf(k)), subcontent.getLong(k));
                } catch (NumberFormatException x) {}
            }
            instance.setType(VoxelType.values()[((CompoundNBT) nbt).getInt("type")]);

            //Only validate on server.
            if(Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
                instance.validate();
        }
    }
}
