package nl.dgoossens.chiselsandbits2.client.render.model;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraftforge.client.extensions.IForgeBakedModel;

public abstract class BaseBakedBlockModel extends BaseBakedPerspectiveModel implements IBakedModel, IForgeBakedModel {
    @Override
    final public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    final public boolean isGui3d() {
        return true;
    }

    @Override
    final public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    final public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

}
