package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BagContainer;

public class ModContainers {
    public final ContainerType<BagContainer> BIT_BAG = (ContainerType<BagContainer>) register("bit_bag", BagContainer::new);

    public <T extends Container> ContainerType<T> register(String key, ContainerType.IFactory<T> factory) {
        ContainerType<T> t = (ContainerType<T>) new ContainerType<>(factory).setRegistryName(ChiselsAndBits2.MOD_ID, key);
        ForgeRegistries.CONTAINERS.register(t);
        return t;
    }
}
