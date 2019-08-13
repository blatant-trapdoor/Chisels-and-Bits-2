package nl.dgoossens.chiselsandbits2.client.render.models;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.IModelData;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModelCombined extends BaseBakedBlockModel {
    private static final Random RAND = new Random();

    IBakedModel[] merged;

    List<BakedQuad>[] face;
    List<BakedQuad> generic;

    @SuppressWarnings("unchecked")
    public ModelCombined(
            final IBakedModel... args) {
        face = new ArrayList[Direction.values().length];

        generic = new ArrayList<>();
        for(final Direction f : Direction.values())
            face[f.ordinal()] = new ArrayList<>();

        merged = args;
        for(final IBakedModel m : merged) {
            generic.addAll(m.getQuads(null, null, RAND));
            for(final Direction f : Direction.values()) {
                face[f.ordinal()].addAll(m.getQuads(null, f, RAND));
            }
        }
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        for(final IBakedModel a : merged) {
            return a.getParticleTexture();
        }
        return ChiselsAndBits2.getClient().getMissingIcon();
    }

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
        if(side != null) {
            return face[side.ordinal()];
        }
        return generic;
    }
}
