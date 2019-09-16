package nl.dgoossens.chiselsandbits2.client.render.models;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockSmartModel;
import nl.dgoossens.chiselsandbits2.api.ICacheClearable;
import nl.dgoossens.chiselsandbits2.client.render.ter.GfxRenderState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmartModelManager {
    private final HashMap<ResourceLocation, IBakedModel> models = new HashMap<>();
    private final List<ICacheClearable> clearable = new ArrayList<>();

    public SmartModelManager() {
        ChiseledBlockSmartModel smartModel = new ChiseledBlockSmartModel();
        add(new ResourceLocation(ChiselsAndBits2.MOD_ID, "models/item/chiseled_block"), smartModel);
        add(new ResourceLocation(ChiselsAndBits2.MOD_ID, "chiseled_block"), smartModel);
        ChiselsAndBits2.getInstance().addClearable(smartModel);
    }

    private void add(
            final ResourceLocation modelLocation,
            final IBakedModel modelGen) {
        if(modelLocation==null) return;
        final ResourceLocation second = new ResourceLocation(modelLocation.getNamespace(), modelLocation.getPath().substring(1 + modelLocation.getPath().lastIndexOf('/')));

        if(modelGen instanceof ICacheClearable)
            clearable.add((ICacheClearable) modelGen);

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
        ChiselsAndBits2.getInstance().clearCache();
    }

    @SubscribeEvent
    public void onModelBakeEvent(final ModelBakeEvent event) {
        for(final ICacheClearable c : clearable)
            c.clearCache();

        for(final ResourceLocation rl : models.keySet())
            event.getModelRegistry().put(rl, models.get(rl));
    }
}
