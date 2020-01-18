package nl.dgoossens.chiselsandbits2.client.render.chiseledblock.model;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.model.BaseSmartModel;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelNeighborRenderTracker;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class ChiseledBlockSmartModel extends BaseSmartModel {
    @Override
    public IBakedModel handleBlockState(final BlockState myState, final Random rand, @Nonnull final IModelData modelData) {
        VoxelBlobStateReference data = modelData.getData(ChiseledBlockTileEntity.VOXEL_DATA);
        if (data == null) data = new VoxelBlobStateReference();

        VoxelNeighborRenderTracker rTracker = modelData.getData(ChiseledBlockTileEntity.NEIGHBOUR_RENDER_TRACKER);
        if (rTracker == null)
            rTracker = new VoxelNeighborRenderTracker(null, null);

        try {
            return ChiselsAndBits2.getInstance().getClient().getRenderingManager().getCachedModel(data, rTracker.getRenderState());
        } catch(ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack, final World world, final LivingEntity entity) {
        try {
            return ChiselsAndBits2.getInstance().getClient().getRenderingManager().getItemModel(stack);
        } catch(ExecutionException x) {
            x.printStackTrace();
            return null;
        }
    }
}
