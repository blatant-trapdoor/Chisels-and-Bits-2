package nl.dgoossens.chiselsandbits2;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nl.dgoossens.chiselsandbits2.api.bit.BitStorage;
import nl.dgoossens.chiselsandbits2.api.ChiselsAndBitsAPI;
import nl.dgoossens.chiselsandbits2.client.ClientSide;
import nl.dgoossens.chiselsandbits2.client.render.models.SmartModelManager;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BitStorageImpl;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapability;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselsAndBitsAPIImpl;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.common.registry.ModBlocks;
import nl.dgoossens.chiselsandbits2.common.registry.ModConfiguration;
import nl.dgoossens.chiselsandbits2.common.registry.ModItems;
import nl.dgoossens.chiselsandbits2.common.registry.ModKeybindings;
import nl.dgoossens.chiselsandbits2.common.registry.ModStatistics;

@Mod(ChiselsAndBits2.MOD_ID)
public class ChiselsAndBits2 {
    public static final String MOD_ID = "chiselsandbits2";

    private static ChiselsAndBits2 instance;

    private final ModItems ITEMS;
    private final ModBlocks BLOCKS;
    private final ModConfiguration CONFIGURATION;
    private final NetworkRouter NETWORK_ROUTER;
    private final ChiselsAndBitsAPI API;
    private final SmartModelManager SMART_MODEL_MANAGER;
    private final ModStatistics STATISTICS;
    private ClientSide CLIENT;
    private ModKeybindings KEYBINDINGS;

    public ChiselsAndBits2() {
        instance = this;
        API = new ChiselsAndBitsAPIImpl();
        CONFIGURATION = new ModConfiguration();
        NETWORK_ROUTER = new NetworkRouter();
        ITEMS = new ModItems();
        BLOCKS = new ModBlocks();
        SMART_MODEL_MANAGER = new SmartModelManager();
        STATISTICS = new ModStatistics();

        //Only register the client and keybindings classes when on the CLIENT distribution.
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            CLIENT = new ClientSide();
            KEYBINDINGS = new ModKeybindings();
        });

        //Register to mod bus
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CONFIGURATION.SERVER);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIGURATION.CLIENT);
    }

    public static ChiselsAndBits2 getInstance() {
        return instance;
    }

    // Ran after all registry events have finished.
    private void setup(final FMLCommonSetupEvent event) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> CLIENT::setup);

        //Register event busses
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(NETWORK_ROUTER);

        FMLJavaModLoadingContext.get().getModEventBus().register(SMART_MODEL_MANAGER);
        NETWORK_ROUTER.init();
        CapabilityManager.INSTANCE.register(BitStorage.class, new StorageCapability(), BitStorageImpl::new);
    }

    public ChiselsAndBitsAPI getAPI() {
        return API;
    }

    public ModItems getItems() {
        return ITEMS;
    }

    public ModBlocks getBlocks() {
        return BLOCKS;
    }

    public ClientSide getClient() {
        return CLIENT;
    }

    public NetworkRouter getNetworkRouter() {
        return NETWORK_ROUTER;
    }

    public ModConfiguration getConfig() {
        return CONFIGURATION;
    }

    public ModKeybindings getKeybindings() {
        return KEYBINDINGS;
    }

    public SmartModelManager getModelManager() {
        return SMART_MODEL_MANAGER;
    }

    public ModStatistics getStatistics() {
        return STATISTICS;
    }

    /**
     * Temporary way to determine whether or not unfinished features should be shown.
     */
    @Deprecated
    public static boolean showUnfinishedFeatures() {
        return !getInstance().getConfig().disableUnfinishedFeatures.get();
    }
}
