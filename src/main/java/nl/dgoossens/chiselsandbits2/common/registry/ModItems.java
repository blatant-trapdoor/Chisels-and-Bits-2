package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.items.PatternItem;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public final Item CHISEL = new ChiselItem(new Item.Properties().maxDamage(238).group(ModItemGroups.CHISELS_AND_BITS2));
    public final Item PATTERN = new PatternItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));

    @SubscribeEvent
    public static void onItemRegistry(final RegistryEvent.Register<Item> e) {
        //Register all items in this class automatically.
        ModItems k = ChiselsAndBits2.getItems();
        registerAll(e, k, Item.class);
    }

    /**
     * Internal method to automatically register all fields in the class to the registry.
     */
    static <T extends IForgeRegistryEntry<T>> void registerAll(final RegistryEvent.Register<T> register, final Object k, final Class<T> type) {
        Stream.of(k.getClass().getFields()).parallel()
                .filter(i -> type.isAssignableFrom(i.getType()))
                .forEach(i -> {
                    try {
                        register.getRegistry().register(((ForgeRegistryEntry<T>) i.get(k)).setRegistryName(ChiselsAndBits2.MOD_ID, i.getName().toLowerCase()));
                    } catch(IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                });
    }
}
