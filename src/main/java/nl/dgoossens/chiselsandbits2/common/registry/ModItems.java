package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.DyedItemColour;
import nl.dgoossens.chiselsandbits2.api.item.IColourable;
import nl.dgoossens.chiselsandbits2.common.items.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    //Cached data to make colour lookups faster.
    private Map<Class<? extends IColourable>, Map<DyedItemColour, IColourable>> colourables = new HashMap<>();
    private Map<Class<? extends IColourable>, String> colourableRegistryNames = new HashMap<>();

    public final Item CHISEL = new ChiselItem(new Item.Properties().maxDamage(-1).group(ModItemGroups.CHISELS_AND_BITS2));
    public Item PATTERN;
    public Item PATTERN_BINDER;
    public Item SAW;
    public final Item TAPE_MEASURE = new TapeMeasureItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
    public Item WRENCH = new WrenchItem(new Item.Properties().maxDamage(1536).group(ModItemGroups.CHISELS_AND_BITS2));
    public Item BLUEPRINT;
    public final Item MORPHING_BIT = new MorphingBitItem(new Item.Properties().group(ModItemGroups.CHISELS_AND_BITS2));

    public final Item BIT_BAG = new BitBagItem();
    public Item BIT_BEAKER;
    
    public Item OAK_PALETTE;
    public Item SPRUCE_PALETTE;
    public Item BIRCH_PALETTE;
    public Item JUNGLE_PALETTE;
    public Item ACACIA_PALETTE;
    public Item DARK_OAK_PALETTE;
    
    public ModItems() {
        //Temporarily initialise in constructor but only if the feature is finished
        if(ChiselsAndBits2.showUnfinishedFeatures()) {
            PATTERN = new PatternItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
            SAW = new SawItem(new Item.Properties().maxDamage(238).group(ModItemGroups.CHISELS_AND_BITS2));
            BLUEPRINT = new BlueprintItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
            PATTERN_BINDER = new Item(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
            OAK_PALETTE = new PaletteItem();
            SPRUCE_PALETTE = new PaletteItem();
            BIRCH_PALETTE = new PaletteItem();
            JUNGLE_PALETTE = new PaletteItem();
            ACACIA_PALETTE = new PaletteItem();
            DARK_OAK_PALETTE = new PaletteItem();
            BIT_BEAKER = new BitBeakerItem();
            buildColourable(BitBeakerItem.class, BitBeakerItem::new, "_TINTED_BIT_BEAKER");
        }

        //Build the colourables
        buildColourable(BitBagItem.class, BitBagItem::new, "_BIT_BAG");
    }

    /**
     * Builds a set of colourable items from a class that implements IColourable.
     * Automatically adds recipe.
     * @param registryName Suffix to the registry name, will be prefixed with colour. E.g. _TINTED_BIT_BEAKER or _BIT_BAG, starting with _ is advised
     */
    public <T extends IColourable> void buildColourable(Class<T> klass, Supplier<T> constructor, String registryName) {
        Map<DyedItemColour, IColourable> map = new HashMap<>();
        for(DyedItemColour c : DyedItemColour.values())
            map.put(c, constructor.get());
        colourables.put(klass, map);
        colourableRegistryNames.put(klass, registryName);
    }
    
    /**
     * Get the rgb colour of the bit bag.
     */
    public int getColourableColour(final ItemStack stack) {
        //Optimized the fudge out of this method because ItemColors get called every frame.
        switch(stack.getItem().getRegistryName().getPath().toUpperCase()) {
            case "WHITE_BIT_BAG": return 16383998;
            case "ORANGE_BIT_BAG": return 16351261;
            case "MAGENTA_BIT_BAG": return 13061821;
            case "LIGHT_BLUE_BIT_BAG": return 3847130;
            case "YELLOW_BIT_BAG": return 16701501;
            case "LIME_BIT_BAG": return 8439583;
            case "PINK_BIT_BAG": return 15961002;
            case "GRAY_BIT_BAG": return 4673362;
            case "LIGHT_GRAY_BIT_BAG": return 10329495;
            case "CYAN_BIT_BAG": return 1481884;
            case "PURPLE_BIT_BAG": return 8991416;
            case "BLUE_BIT_BAG": return 3949738;
            case "BROWN_BIT_BAG": return 8606770;
            case "GREEN_BIT_BAG": return 6192150;
            case "RED_BIT_BAG": return 11546150;
            case "BLACK_BIT_BAG": return 1908001;
        }
        return -1;
    }

    /**
     * Internal method used by recipe generators.
     */
    public <T extends Item> T getColouredItem(final Class<T> klass, final DyedItemColour colour) {
        if(!(Item.class.isAssignableFrom(klass))) throw new UnsupportedOperationException("An IColourable must also extend Item!");
        return (T) colourables.getOrDefault(klass, new HashMap<>()).get(colour);
    }

    /**
     * Internal method used by recipe generators.
     */
    public PaletteItem getPalette(final String fieldName) {
        try {
            for (Field f : getClass().getFields()) {
                if(fieldName.equals(f.getName()))
                    return (PaletteItem) f.get(this);
            }
        } catch(Exception x) {}
        return null;
    }

    /**
     * Internal method to automatically register all fields in the class to the registry.
     */
    static <T extends IForgeRegistryEntry<T>> void registerAll(final RegistryEvent.Register<T> register, final Object k, final Class<T> type) {
        for (Field f : k.getClass().getFields()) {
            if (!type.isAssignableFrom(f.getType())) continue;
            try {
                ForgeRegistryEntry<T> g = (ForgeRegistryEntry<T>) f.get(k);
                if(g!=null)
                    register.getRegistry().register(g.setRegistryName(ChiselsAndBits2.MOD_ID,
                        f.getName().toLowerCase()));
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public static void onItemRegistry(final RegistryEvent.Register<Item> e) {
        //Register all items in this class automatically.
        registerAll(e, ChiselsAndBits2.getInstance().getItems(), Item.class);

        //Register IColourables
        for(Map.Entry<Class<? extends IColourable>, Map<DyedItemColour, IColourable>> r : ChiselsAndBits2.getInstance().getItems().colourables.entrySet()) {
            for(Map.Entry<DyedItemColour, IColourable> en : r.getValue().entrySet()) {
                Item i = (Item) en.getValue();
                e.getRegistry().register(i.setRegistryName(ChiselsAndBits2.MOD_ID, en.getKey().name()+ChiselsAndBits2.getInstance().getItems().colourableRegistryNames.get(r.getKey()).toLowerCase()));
            }
        }
    }
}
