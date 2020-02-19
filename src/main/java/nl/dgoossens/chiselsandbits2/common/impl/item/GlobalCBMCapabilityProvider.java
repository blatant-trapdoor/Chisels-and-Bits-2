package nl.dgoossens.chiselsandbits2.common.impl.item;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import nl.dgoossens.chiselsandbits2.api.bit.BitStorage;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BitStorageImpl;
import nl.dgoossens.chiselsandbits2.common.items.StorageItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GlobalCBMCapabilityProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(ItemModeWrapper.class)
    public static final Capability<ItemModeWrapper> GLOBAL_CBM = null;

    private ItemModeWrapper instance = new ItemModeWrapper();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == GLOBAL_CBM ? LazyOptional.of(() -> (T) this.instance) : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return GLOBAL_CBM.writeNBT(instance, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        GLOBAL_CBM.readNBT(instance, null, nbt);
    }
}
