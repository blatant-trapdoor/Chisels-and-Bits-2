package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.gui.BitBagScreen;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BagContainer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {
    public final ContainerType<BagContainer> BIT_BAG = buildType("bit_bag", BagContainer::new);

    public ModContainers() {
        //Register container screens where applicable
        ScreenManager.registerFactory(BIT_BAG, BitBagScreen::new);
    }

    public <T extends Container> ContainerType<T> buildType(String key, ContainerType.IFactory<T> factory) {
        return (ContainerType<T>) new ContainerType<>(factory).setRegistryName(ChiselsAndBits2.MOD_ID, key);
    }

    @SubscribeEvent
    public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> e) {
        final ModContainers c = ChiselsAndBits2.getInstance().getContainers();
        e.getRegistry().register(c.BIT_BAG);
    }
}
