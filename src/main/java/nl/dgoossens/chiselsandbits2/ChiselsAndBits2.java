package nl.dgoossens.chiselsandbits2;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLModIdMappingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import nl.dgoossens.chiselsandbits2.client.ClientSide;
import nl.dgoossens.chiselsandbits2.client.render.ICacheClearable;
import nl.dgoossens.chiselsandbits2.client.render.models.SmartModelManager;
import nl.dgoossens.chiselsandbits2.common.registry.ModBlocks;
import nl.dgoossens.chiselsandbits2.common.registry.ModItems;
import nl.dgoossens.chiselsandbits2.common.utils.ModelUtil;
import nl.dgoossens.chiselsandbits2.network.NetworkRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(ChiselsAndBits2.MOD_ID)
public class ChiselsAndBits2 {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "chiselsandbits2";

    private static ChiselsAndBits2 instance;

    private final ModItems ITEMS = new ModItems();
    private final ModBlocks BLOCKS = new ModBlocks();
    private final ClientSide CLIENT = new ClientSide();
    private final SmartModelManager SMART_MODEL_MANAGER;
    private final NetworkRouter NETWORK_ROUTER = new NetworkRouter();

    public ChiselsAndBits2() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
        SMART_MODEL_MANAGER = new SmartModelManager();
    }

    public static ChiselsAndBits2 getInstance() { return instance; }
    public static ModItems getItems() { return getInstance().ITEMS; }
    public static ModBlocks getBlocks() { return getInstance().BLOCKS; }
    public static ClientSide getClient() { return getInstance().CLIENT; }
    /**
     * Method to get one-self registered into the bus. Used internally.
     * Deprecated to discourage usage!
     */
    @Deprecated
    public static void registerWithBus(Object tar) {
        MinecraftForge.EVENT_BUS.register(tar);
    }

    // Ran after all registry events have finished.
    private void setup(final FMLCommonSetupEvent event) {
        if(FMLEnvironment.dist.isClient()) {
            CLIENT.setup(event);

            //Register client-only event busses
            MinecraftForge.EVENT_BUS.register(CLIENT);
        }

        //Register event busses
        MinecraftForge.EVENT_BUS.register(SMART_MODEL_MANAGER);
        MinecraftForge.EVENT_BUS.register(NETWORK_ROUTER);
    }

    boolean idsHaveBeenMapped = false;
    List<ICacheClearable> cacheClearables = new ArrayList<ICacheClearable>();

    @SubscribeEvent
    public void idsMapped(final FMLModIdMappingEvent event) {
        idsHaveBeenMapped = true;
        //BlockBitInfo.recalculateFluidBlocks();
        clearCache();
        new ModelUtil().clearCache();
    }

    public void clearCache() {
        if (idsHaveBeenMapped) {
            for(final ICacheClearable clearable : cacheClearables)
                clearable.clearCache();

            //TODO addClearable(UndoTracker.getInstance());
            //TODO VoxelBlob.clearCache();
        }
    }

    public void addClearable(final ICacheClearable cache) {
        if (!cacheClearables.contains(cache) )
            cacheClearables.add(cache);
    }
}
