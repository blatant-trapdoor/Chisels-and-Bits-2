package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import nl.dgoossens.chiselsandbits2.api.BeakerStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BeakerCapabilityProvider implements ICapabilitySerializable<INBT> {
    @CapabilityInject(BeakerStorage.class)
    public static final Capability<BeakerStorage> BEAKER_STORAGE = null;

    private BeakerStorage instance = new BeakerStorageImpl();

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == BEAKER_STORAGE ? LazyOptional.of(() -> (T) this.instance) : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        return BEAKER_STORAGE.getStorage().writeNBT(BEAKER_STORAGE, this.instance, null);
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        BEAKER_STORAGE.getStorage().readNBT(BEAKER_STORAGE, this.instance, null, nbt);
    }
}
