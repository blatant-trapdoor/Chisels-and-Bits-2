package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.MenuAction;
import nl.dgoossens.chiselsandbits2.common.items.*;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    public final Item CHISEL = new ChiselItem(new Item.Properties().maxDamage(238).group(ModItemGroups.CHISELS_AND_BITS2));
    public final Item PATTERN = new PatternItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
    public final Item SAW = new SawItem(new Item.Properties().maxDamage(238).group(ModItemGroups.CHISELS_AND_BITS2));
    public final Item TAPE_MEASURE = new TapeMeasureItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
    public final Item WRENCH = new WrenchItem(new Item.Properties().maxDamage(238).group(ModItemGroups.CHISELS_AND_BITS2));

    //all of the coloured bit bags
    public final Item BIT_BAG = new BitBagItem();
    public final Item WHITE_BIT_BAG = new BitBagItem();
    public final Item ORANGE_BIT_BAG = new BitBagItem();
    public final Item MAGENTA_BIT_BAG = new BitBagItem();
    public final Item LIGHT_BLUE_BIT_BAG = new BitBagItem();
    public final Item YELLOW_BIT_BAG = new BitBagItem();
    public final Item LIME_BIT_BAG = new BitBagItem();
    public final Item PINK_BIT_BAG = new BitBagItem();
    public final Item GRAY_BIT_BAG = new BitBagItem();
    public final Item LIGHT_GRAY_BIT_BAG = new BitBagItem();
    public final Item CYAN_BIT_BAG = new BitBagItem();
    public final Item PURPLE_BIT_BAG = new BitBagItem();
    public final Item BLUE_BIT_BAG = new BitBagItem();
    public final Item BROWN_BIT_BAG = new BitBagItem();
    public final Item GREEN_BIT_BAG = new BitBagItem();
    public final Item RED_BIT_BAG = new BitBagItem();
    public final Item BLACK_BIT_BAG = new BitBagItem();

    //Some more regular items (we put these after the bit bags because they are new and people who know C&B will notice them)
    public final Item BIT_BEAKER = new BitBeakerItem();
    public final Item MALLET = new MalletItem(new Item.Properties().maxDamage(238).group(ModItemGroups.CHISELS_AND_BITS2));
    public final Item BLUEPRINT = new BlueprintItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
    public final Item PALETTE = new PaletteItem();

    @SubscribeEvent
    public static void onItemRegistry(final RegistryEvent.Register<Item> e) {
        //Register all items in this class automatically.
        registerAll(e, ChiselsAndBits2.getInstance().getItems(), Item.class);
    }

    /**
     * Internal method to automatically register all fields in the class to the registry.
     */
    static <T extends IForgeRegistryEntry<T>> void registerAll(final RegistryEvent.Register<T> register, final Object k, final Class<T> type) {
        for (Field f : k.getClass().getFields()) {
            if (!type.isAssignableFrom(f.getType())) continue;
            try {
                register.getRegistry().register(((ForgeRegistryEntry<T>) f.get(k)).setRegistryName(ChiselsAndBits2.MOD_ID,
                        f.getName().toLowerCase()));
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    /**
     * Get the colour of the bit bag, we hijack MenuAction's colour values.
     */
    public MenuAction getBitBagColour(final ItemStack stack) {
        if (!(stack.getItem() instanceof BitBagItem)) return null;
        for (Field f : getClass().getFields()) {
            try {
                if (!f.getName().contains("_BIT_BAG") || !stack.getItem().equals(f.get(this))) continue;
                return MenuAction.valueOf(f.getName().substring(0, f.getName().length() - "_BIT_BAG".length()));
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return null;
    }
}
