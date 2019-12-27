package nl.dgoossens.chiselsandbits2;

import net.minecraft.state.properties.BlockStateProperties;
import net.minecraftforge.api.distmarker.Dist;
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
import nl.dgoossens.chiselsandbits2.client.UndoTracker;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BitStorageImpl;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapability;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselsAndBitsAPIImpl;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.common.registry.*;

@Mod(ChiselsAndBits2.MOD_ID)
public class ChiselsAndBits2 {
    public static final String MOD_ID = "chiselsandbits2";

    private static ChiselsAndBits2 instance;

    private final ModItems ITEMS;
    private final ModBlocks BLOCKS;
    private final ModConfiguration CONFIGURATION;
    private final NetworkRouter NETWORK_ROUTER;
    private final ChiselsAndBitsAPI API;
    private final ModStatistics STATISTICS;
    private final ModRecipes RECIPES;
    private final UndoTracker UNDO;
    private ClientSide CLIENT;
    private ModKeybindings KEYBINDINGS;

    public ChiselsAndBits2() {
        instance = this;
        API = new ChiselsAndBitsAPIImpl();
        CONFIGURATION = new ModConfiguration();
        NETWORK_ROUTER = new NetworkRouter();
        ITEMS = new ModItems();
        BLOCKS = new ModBlocks();
        STATISTICS = new ModStatistics();
        RECIPES = new ModRecipes();
        UNDO = new UndoTracker();

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

        //Register things
        CapabilityManager.INSTANCE.register(BitStorage.class, new StorageCapability(), BitStorageImpl::new);
        NETWORK_ROUTER.init();

        //Setup vanilla restrictions
        getAPI().getRestrictions().restrictBlockStateProperty(BlockStateProperties.SNOWY, false, true); //Make all snowy grass not snowy automatically
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

    public ModStatistics getStatistics() {
        return STATISTICS;
    }

    public ModRecipes getRecipes() {
        return RECIPES;
    }

    public UndoTracker getUndoTracker() {
        return UNDO;
    }

    /**
     * Temporary way to determine whether or not unfinished features should be shown.
     */
    @Deprecated
    public static boolean showUnfinishedFeatures() {
        return !getInstance().getConfig().disableUnfinishedFeatures.get();
    }
}
