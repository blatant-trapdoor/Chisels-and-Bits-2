package nl.dgoossens.chiselsandbits2.client.render.models;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLModIdMappingEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockSmartModel;
import nl.dgoossens.chiselsandbits2.client.render.ter.GfxRenderState;
import nl.dgoossens.chiselsandbits2.common.utils.ModelUtil;

import java.util.HashMap;

public class SmartModelManager {
    private final HashMap<ResourceLocation, IBakedModel> models = new HashMap<>();

    public SmartModelManager() {
        ChiseledBlockSmartModel smartModel = new ChiseledBlockSmartModel();
        add(new ResourceLocation(ChiselsAndBits2.MOD_ID, "models/item/chiseled_block"), smartModel);
        add(new ResourceLocation(ChiselsAndBits2.MOD_ID, "chiseled_block"), smartModel);
        CacheType.DEFAULT.register(smartModel);
        CacheType.DEFAULT.register(new ModelUtil());
    }

    private void add(
            final ResourceLocation modelLocation,
            final IBakedModel modelGen) {
        if (modelLocation == null) return;
        final ResourceLocation second = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath().substring(1 + modelLocation.getPath().lastIndexOf('/')));

        if (modelGen instanceof CacheClearable)
            CacheType.MODEL.register((CacheClearable) modelGen);

        models.put(modelLocation, modelGen);
        models.put(second, modelGen);

        models.put(new ModelResourceLocation(modelLocation, "normal"), modelGen);
        models.put(new ModelResourceLocation(second, "normal"), modelGen);

        models.put(new ModelResourceLocation(modelLocation, "inventory"), modelGen);
        models.put(new ModelResourceLocation(second, "inventory"), modelGen);
    }

    @SubscribeEvent
    public void textureStitchEvent(final TextureStitchEvent.Post e) {
        GfxRenderState.gfxRefresh++;
        CacheType.DEFAULT.call();
    }

    @SubscribeEvent
    public void onModelBakeEvent(final ModelBakeEvent event) {
        CacheType.MODEL.call();
        for (final ResourceLocation rl : models.keySet())
            event.getModelRegistry().put(rl, models.get(rl));
    }

    @SubscribeEvent
    public void idsMapped(final FMLModIdMappingEvent event) {
        CacheType.DEFAULT.call();
    }
}
