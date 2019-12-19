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
import nl.dgoossens.chiselsandbits2.client.render.MorphingBitSmartModel;
import nl.dgoossens.chiselsandbits2.client.render.ter.GfxRenderState;
import nl.dgoossens.chiselsandbits2.common.utils.ModelUtil;

import java.util.HashMap;

public class SmartModelManager {
    @SubscribeEvent
    public void textureStitchEvent(final TextureStitchEvent.Post e) {
        GfxRenderState.gfxRefresh++;
        CacheType.DEFAULT.call();
    }

    @SubscribeEvent
    public void onModelBakeEvent(final ModelBakeEvent event) {
        CacheType.MODEL.call();

        //Chiseled Block
        ChiseledBlockSmartModel smartModel = new ChiseledBlockSmartModel();
        event.getModelRegistry().put(new ModelResourceLocation(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK.getRegistryName(), ""), smartModel);
        event.getModelRegistry().put(new ModelResourceLocation(ChiselsAndBits2.getInstance().getBlocks().CHISELED_BLOCK.getRegistryName(), "inventory"), smartModel);

        //Morphing Bit
        MorphingBitSmartModel morphingModel = new MorphingBitSmartModel();
        event.getModelRegistry().put(new ModelResourceLocation(ChiselsAndBits2.getInstance().getItems().MORPHING_BIT.getRegistryName(), ""), morphingModel);
        event.getModelRegistry().put(new ModelResourceLocation(ChiselsAndBits2.getInstance().getItems().MORPHING_BIT.getRegistryName(), "inventory"), morphingModel);

        CacheType.DEFAULT.register(new ModelUtil());
    }

    @SubscribeEvent
    public void idsMapped(final FMLModIdMappingEvent event) {
        CacheType.DEFAULT.call();
    }
}
