package nl.dgoossens.chiselsandbits2.client.render.models;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class BaseSmartModel implements IBakedModel, IForgeBakedModel {
    private final ItemOverrideList overrides;

    public BaseSmartModel() {
        overrides = new OverrideHelper(this);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
        return getParticleTexture();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        final TextureAtlasSprite sprite = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.STONE.getDefaultState());
        return sprite;
    }

    @Override
    public List<BakedQuad> getQuads(final BlockState state, final Direction side, final Random rand, @Nonnull final IModelData modelData) {
        final IBakedModel model = handleBlockState(state, rand, modelData);
        return model.getQuads(state, side, rand, modelData);
    }

    @Override
    public List<BakedQuad> getQuads(final BlockState state, final Direction side, final Random rand) {
        return Collections.emptyList();
    }

    public IBakedModel handleBlockState(final BlockState state, final Random rand, @Nonnull final IModelData modelData) {
        return NullBakedModel.instance;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return overrides;
    }

    public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack, final World world, final LivingEntity entity) {
        return originalModel;
    }

    private static class OverrideHelper extends ItemOverrideList {
        final BaseSmartModel parent;

        public OverrideHelper(final BaseSmartModel p) {
            super();
            parent = p;
        }

        @Nullable
        public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable LivingEntity entity) {
            return parent.handleItemState(originalModel, stack, world, entity);
        }
    }
}
