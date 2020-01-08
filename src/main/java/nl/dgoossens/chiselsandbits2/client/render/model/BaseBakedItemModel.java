package nl.dgoossens.chiselsandbits2.client.render.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.util.Direction;
import net.minecraftforge.client.extensions.IForgeBakedModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class BaseBakedItemModel extends BaseBakedPerspectiveModel implements IBakedModel, IForgeBakedModel {
    protected ArrayList<BakedQuad> list = new ArrayList<>();

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
    public List<BakedQuad> getQuads(final BlockState state, final Direction side, final Random rand) {
        if (side != null) return Collections.emptyList();
        return list;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
