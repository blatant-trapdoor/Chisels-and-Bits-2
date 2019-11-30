package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.items.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModItems {
    //Cached data to make colour lookups faster.
    private Map<BagBeakerColours, BitBagItem> bags = new HashMap<>();
    private Map<BagBeakerColours, BitBeakerItem> beakers = new HashMap<>();

    public final Item CHISEL = new ChiselItem(new Item.Properties().maxDamage(-1).group(ModItemGroups.CHISELS_AND_BITS2));
    public Item PATTERN;
    public Item SAW;
    public final Item TAPE_MEASURE = new TapeMeasureItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
    public Item WRENCH = new WrenchItem(new Item.Properties().maxDamage(1536).group(ModItemGroups.CHISELS_AND_BITS2));
    public Item MALLET;
    public Item BLUEPRINT;
    public final Item MORPHING_BIT = new MorphingBitItem(new Item.Properties().group(ModItemGroups.CHISELS_AND_BITS2));
    public Item PATTERN_BOOK;

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

    public Item BIT_BEAKER;
    public Item WHITE_TINTED_BIT_BEAKER;
    public Item ORANGE_TINTED_BIT_BEAKER;
    public Item MAGENTA_TINTED_BIT_BEAKER;
    public Item LIGHT_BLUE_TINTED_BIT_BEAKER;
    public Item YELLOW_TINTED_BIT_BEAKER;
    public Item LIME_TINTED_BIT_BEAKER;
    public Item PINK_TINTED_BIT_BEAKER;
    public Item GRAY_TINTED_BIT_BEAKER;
    public Item LIGHT_GRAY_TINTED_BIT_BEAKER;
    public Item CYAN_TINTED_BIT_BEAKER;
    public Item PURPLE_TINTED_BIT_BEAKER;
    public Item BLUE_TINTED_BIT_BEAKER;
    public Item BROWN_TINTED_BIT_BEAKER;
    public Item GREEN_TINTED_BIT_BEAKER;
    public Item RED_TINTED_BIT_BEAKER;
    public Item BLACK_TINTED_BIT_BEAKER;
    
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
            MALLET = new MalletItem(new Item.Properties().maxDamage(238).group(ModItemGroups.CHISELS_AND_BITS2));
            BLUEPRINT = new BlueprintItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
            PATTERN_BOOK = new Item(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
            OAK_PALETTE = new PaletteItem();
            SPRUCE_PALETTE = new PaletteItem();
            BIRCH_PALETTE = new PaletteItem();
            JUNGLE_PALETTE = new PaletteItem();
            ACACIA_PALETTE = new PaletteItem();
            DARK_OAK_PALETTE = new PaletteItem();
            BIT_BEAKER = new BitBeakerItem();
            WHITE_TINTED_BIT_BEAKER = new BitBeakerItem();
            ORANGE_TINTED_BIT_BEAKER = new BitBeakerItem();
            MAGENTA_TINTED_BIT_BEAKER = new BitBeakerItem();
            LIGHT_BLUE_TINTED_BIT_BEAKER = new BitBeakerItem();
            YELLOW_TINTED_BIT_BEAKER = new BitBeakerItem();
            LIME_TINTED_BIT_BEAKER = new BitBeakerItem();
            PINK_TINTED_BIT_BEAKER = new BitBeakerItem();
            GRAY_TINTED_BIT_BEAKER = new BitBeakerItem();
            LIGHT_GRAY_TINTED_BIT_BEAKER = new BitBeakerItem();
            CYAN_TINTED_BIT_BEAKER = new BitBeakerItem();
            PURPLE_TINTED_BIT_BEAKER = new BitBeakerItem();
            BLUE_TINTED_BIT_BEAKER = new BitBeakerItem();
            BROWN_TINTED_BIT_BEAKER = new BitBeakerItem();
            GREEN_TINTED_BIT_BEAKER = new BitBeakerItem();
            RED_TINTED_BIT_BEAKER = new BitBeakerItem();
            BLACK_TINTED_BIT_BEAKER = new BitBeakerItem();
        }

        //Build the caches
        for (Field f : getClass().getFields()) {
            if(f.getName().endsWith("_BIT_BAG")) {
                String val = f.getName().substring(0, f.getName().length() - "_BIT_BAG".length());
                try {
                    bags.put(BagBeakerColours.valueOf(val), (BitBagItem) f.get(this));
                } catch(Exception x) {}
            }
            if(f.getName().endsWith("_TINTED_BIT_BEAKER")) {
                String val = f.getName().substring(0, f.getName().length() - "_TINTED_BIT_BEAKER".length());
                try {
                   beakers.put(BagBeakerColours.valueOf(val), (BitBeakerItem) f.get(this));
                } catch(Exception x) {}
            }
        }
    }
    
    /**
     * Get the rgb colour of the bit bag.
     */
    public int getBagBeakerColour(final ItemStack stack) {
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
    public BitBagItem getBitBag(final BagBeakerColours colour) {
        return bags.get(colour);
    }

    /**
     * Internal method used by recipe generators.
     */
    public BitBeakerItem getBitBeaker(final BagBeakerColours colour) {
        return beakers.get(colour);
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
    }
}
