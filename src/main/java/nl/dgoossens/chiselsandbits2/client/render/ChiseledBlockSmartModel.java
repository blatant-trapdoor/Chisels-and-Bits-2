package nl.dgoossens.chiselsandbits2.client.render;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.ForgeConfig;
import nl.dgoossens.chiselsandbits2.client.render.models.BaseSmartModel;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelNeighborRenderTracker;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ChiseledBlockSmartModel extends BaseSmartModel {
    private static ChiseledBlockBaked NULL_MODEL;

    private static final Cache<ModelRenderState, ChiseledBlockBaked> modelCache = CacheBuilder.newBuilder()
            .maximumSize(2000)
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .build();

    private static final LoadingCache<ItemStack, IBakedModel> itemToModel = CacheBuilder.newBuilder()
            .maximumSize(2000)
            .expireAfterAccess(10, TimeUnit.SECONDS)
            .build(new CacheLoader<ItemStack, IBakedModel>() {
                @Override
                public IBakedModel load(ItemStack stack) throws Exception {
                    CompoundNBT c = stack.getTag();
                    if (c == null) c = new CompoundNBT();
                    else c = c.getCompound(ChiselUtil.NBT_BLOCKENTITYTAG);

                    byte[] vdata = c.getByteArray(NBTBlobConverter.NBT_VERSIONED_VOXEL);
                    VoxelBlobStateReference state = c.contains(NBTBlobConverter.NBT_VERSIONED_VOXEL) ? new VoxelBlobStateReference(vdata) : new VoxelBlobStateReference();

                    return getCachedModel(state, null, DefaultVertexFormats.ITEM);
                }
            });

    public static ChiseledBlockBaked getCachedModel(final ChiseledBlockTileEntity te) {
        try {
            return getCachedModel(te.getVoxelReference(), te.getRenderTracker().getRenderState(), getModelFormat());
        } catch(ExecutionException x) {
            x.printStackTrace();
            return null;
        }
    }

    private static VertexFormat getModelFormat() {
        return !ForgeConfig.CLIENT.forgeLightPipelineEnabled.get() ? DefaultVertexFormats.ITEM : ChiselsAndBitsBakedQuad.VERTEX_FORMAT;
    }

    public static boolean isInvalid(final ModelRenderState mrs) {
        //Invalidate this cache if the render tracker changed
        if (mrs != null && mrs.isDirty()) {
            modelCache.invalidate(mrs);
            return true;
        }
        return false;
    }

    private static ChiseledBlockBaked getCachedModel(final VoxelBlobStateReference reference, final ModelRenderState mrs, final VertexFormat format) throws ExecutionException {
        if (reference == null) {
            if(NULL_MODEL == null) NULL_MODEL = new ChiseledBlockBaked(null, new ModelRenderState(), getModelFormat());
            return NULL_MODEL;
        }

        if (format == getModelFormat() && mrs != null)
            return modelCache.get(mrs, () -> new ChiseledBlockBaked(reference, mrs, format));

        return new ChiseledBlockBaked(reference, mrs, format);
    }

    @Override
    public IBakedModel handleBlockState(final BlockState myState, final Random rand, @Nonnull final IModelData modelData) {
        VoxelBlobStateReference data = modelData.getData(ChiseledBlockTileEntity.VOXEL_DATA);
        if (data == null) data = new VoxelBlobStateReference();

        VoxelNeighborRenderTracker rTracker = modelData.getData(ChiseledBlockTileEntity.NEIGHBOUR_RENDER_TRACKER);
        if (rTracker == null)
            rTracker = new VoxelNeighborRenderTracker(null, null);

        try {
            return getCachedModel(data, rTracker.getRenderState(), getModelFormat());
        } catch(ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack, final World world, final LivingEntity entity) {
        try {
            return itemToModel.get(stack);
        } catch(Exception x) {
            x.printStackTrace();
            return null;
        }
    }
}
