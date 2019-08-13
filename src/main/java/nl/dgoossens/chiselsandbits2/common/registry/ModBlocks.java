package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.blocks.BaseBlock;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlock;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;

import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBlocks {
    public final Block CHISELED_BLOCK = new ChiseledBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(50.0F, 1200.0F));
    public final TileEntityType<ChiseledBlockTileEntity> CHISELED_BLOCK_TILE = TileEntityType.Builder.create(ChiseledBlockTileEntity::new, CHISELED_BLOCK).build(null);

    @SubscribeEvent
    public static void onBlockRegistry(final RegistryEvent.Register<Block> e) {
        //Register all blocks in this class automatically.
        ModBlocks k = ChiselsAndBits2.getBlocks();
        ModItems.registerAll(e, k, Block.class);
    }

    @SubscribeEvent
    public static void onItemRegistry(final RegistryEvent.Register<Item> e) {
        //Register all blocks' items in this class automatically.
        ModBlocks k = ChiselsAndBits2.getBlocks();
        Stream.of(k.getClass().getFields()).parallel()
                .filter(block -> Block.class.isAssignableFrom(block.getType()))
                .forEach(block -> {
                    try {
                        Item.Properties properties = new Item.Properties();
                        if(block.get(k) instanceof BaseBlock) {
                            BaseBlock bb = ((BaseBlock) block.get(k));
                            properties = bb.getItemProperties();
                            BlockItem bi = bb.getBlockItem();
                            if(bi!=null) {
                                e.getRegistry().register(bi.setRegistryName(ChiselsAndBits2.MOD_ID, block.getName().toLowerCase()));
                                return;
                            }
                        }
                        e.getRegistry().register(new BlockItem((Block) block.get(k), properties).setRegistryName(ChiselsAndBits2.MOD_ID, block.getName().toLowerCase()));
                    } catch(IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                });
    }

    @SubscribeEvent
    public static void onTileEntityRegistry(final RegistryEvent.Register<TileEntityType<?>> e) {
        //Register all blocks' items in this class automatically.
        ModBlocks k = ChiselsAndBits2.getBlocks();
        Stream.of(k.getClass().getFields()).parallel()
                .filter(tile -> TileEntityType.class.isAssignableFrom(tile.getType()))
                .forEach(tile -> {
                    try {
                        e.getRegistry().register(((TileEntityType<?>) tile.get(k)).setRegistryName(ChiselsAndBits2.MOD_ID, tile.getName().toLowerCase()));
                    } catch(IllegalAccessException ex) {
                        ex.printStackTrace();
                    }
                });
    }
}
