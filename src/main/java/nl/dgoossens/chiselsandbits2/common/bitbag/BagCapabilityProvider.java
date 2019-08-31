package nl.dgoossens.chiselsandbits2.common.bitbag;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import nl.dgoossens.chiselsandbits2.api.BagStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BagCapabilityProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(BagStorage.class)
    public static final Capability<BagStorage> BAG_STORAGE = null;

    private BagStorage instance = new BagStorageImpl();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == BAG_STORAGE ? LazyOptional.of(() -> (T) this.instance) : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return BAG_STORAGE.getStorage().writeNBT(BAG_STORAGE, this.instance, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        BAG_STORAGE.getStorage().readNBT(BAG_STORAGE, this.instance, null, nbt);
    }
}
