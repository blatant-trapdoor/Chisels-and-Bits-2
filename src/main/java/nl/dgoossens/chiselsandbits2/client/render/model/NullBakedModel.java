package nl.dgoossens.chiselsandbits2.client.render.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class NullBakedModel implements IBakedModel, IForgeBakedModel {
    public static final NullBakedModel instance = new NullBakedModel();

    @Override
    public List<BakedQuad> getQuads(
            final BlockState state,
            final Direction side,
            final Random rand,
            @Nonnull final IModelData modelData) {
        return getQuads(state, side, rand);
    }

    @Override
    public List<BakedQuad> getQuads(
            final BlockState state,
            final Direction side,
            final Random rand) {
        return Collections.emptyList();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return ChiselsAndBits2.getInstance().getClient().getMissingIcon();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

}
