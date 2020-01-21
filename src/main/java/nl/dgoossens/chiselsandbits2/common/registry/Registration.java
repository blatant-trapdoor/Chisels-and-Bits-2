package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.gui.BitBagScreen;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BagContainer;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.items.BitBagItem;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.items.ChiseledBlockItem;
import nl.dgoossens.chiselsandbits2.common.items.MorphingBitItem;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;
import nl.dgoossens.chiselsandbits2.common.items.WrenchItem;

public class Registration {
    private final DeferredRegister<Block> BLOCKS = new DeferredRegister<>(ForgeRegistries.BLOCKS, ChiselsAndBits2.MOD_ID);
    private final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, ChiselsAndBits2.MOD_ID);
    private final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = new DeferredRegister<>(ForgeRegistries.TILE_ENTITIES, ChiselsAndBits2.MOD_ID);
    private final DeferredRegister<ContainerType<?>> CONTAINERS = new DeferredRegister<>(ForgeRegistries.CONTAINERS, ChiselsAndBits2.MOD_ID);

    public final RegistryObject<ChiseledBlock> CHISELED_BLOCK = BLOCKS.register("chiseled_block", () -> new ChiseledBlock(Block.Properties.create(Material.ROCK).doesNotBlockMovement().hardnessAndResistance(1.0F, 6.0F)));
    public final RegistryObject<ChiseledBlockItem> CHISELED_BLOCK_ITEM = ITEMS.register("chiseled_block", () ->  new ChiseledBlockItem(CHISELED_BLOCK.get(), new Item.Properties()));
    public final RegistryObject<TileEntityType<ChiseledBlockTileEntity>> CHISELED_BLOCK_TILE = TILE_ENTITIES.register("chiseled_block_tile", () -> TileEntityType.Builder.create(ChiseledBlockTileEntity::new, CHISELED_BLOCK.get()).build(null));

    public final RegistryObject<Item> CHISEL = ITEMS.register("chisel", () -> new ChiselItem(new Item.Properties().maxDamage(-1).group(ModItemGroups.CHISELS_AND_BITS2)));
    public final RegistryObject<Item> TAPE_MEASURE = ITEMS.register("tape_measure", () -> new TapeMeasureItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2)));
    public final RegistryObject<Item> WRENCH = ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties().maxDamage(1536).group(ModItemGroups.CHISELS_AND_BITS2)));

    public final RegistryObject<Item> BIT_BAG = ITEMS.register("bit_bag", BitBagItem::new);
    public final RegistryObject<Item> WHITE_BIT_BAG = ITEMS.register("white_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> ORANGE_BIT_BAG = ITEMS.register("orange_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> MAGENTA_BIT_BAG = ITEMS.register("magenta_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> LIGHT_BLUE_BIT_BAG = ITEMS.register("light_blue_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> YELLOW_BIT_BAG = ITEMS.register("yellow_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> LIME_BIT_BAG = ITEMS.register("lime_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> PINK_BIT_BAG = ITEMS.register("pink_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> LIGHT_GRAY_BIT_BAG = ITEMS.register("light_gray_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> GRAY_BIT_BAG = ITEMS.register("gray_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> CYAN_BIT_BAG = ITEMS.register("cyan_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> PURPLE_BIT_BAG = ITEMS.register("purple_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> BLUE_BIT_BAG = ITEMS.register("blue_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> BROWN_BIT_BAG = ITEMS.register("brown_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> GREEN_BIT_BAG = ITEMS.register("green_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> RED_BIT_BAG = ITEMS.register("red_bit_bag", BitBagItem::new);
    public final RegistryObject<Item> BLACK_BIT_BAG = ITEMS.register("black_bit_bag", BitBagItem::new);

    //Register morphing bit last because it takes a lot of the space
    public final RegistryObject<Item> MORPHING_BIT = ITEMS.register("morphing_bit", () -> new MorphingBitItem(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2)));

    public final RegistryObject<ContainerType<BagContainer>> BIT_BAG_CONTAINER = CONTAINERS.register("bit_bag", () -> IForgeContainerType.create(BagContainer::new));

    public Registration() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());

        //Register container screens where applicable
        ScreenManager.registerFactory(BIT_BAG_CONTAINER.get(), BitBagScreen::new);
    }

    /**
     * Get the rgb colour of a colored bit bag or beaker. This method has been optimized as much as possible
     * as item colours request this and item colours are called every frame for every item.
     */
    public int getColoredItemColor(final ItemStack stack) {
        //Light gray and light blue can't be distinguished by the first 6 chars so we do one of them separate
        if(stack.getItem().getRegistryName().getPath().startsWith("light_gray")) return 10329495;
        switch(stack.getItem().getRegistryName().getPath().substring(0, 3)) { //Only first three characters! (enough to distinguish colours)
            case "whi": return 16383998; //white
            case "ora": return 16351261; //orange
            case "mag": return 13061821; //magenta
            case "lig": return 3847130; //light_blue
            case "yel": return 16701501; //yellow
            case "lim": return 8439583; //lime
            case "pin": return 15961002; //pink
            case "gra": return 4673362; //gray
            case "cya": return 1481884; //cyan
            case "pur": return 8991416; //purple
            case "blu": return 3949738; //blue
            case "bro": return 8606770; //brown
            case "gre": return 6192150; //green
            case "red": return 11546150; //red
            case "bla": return 1908001; //black
        }
        return -1;
    }
}
