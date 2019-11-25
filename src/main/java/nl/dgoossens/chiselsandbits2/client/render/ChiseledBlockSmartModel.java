package nl.dgoossens.chiselsandbits2.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.ForgeMod;
import nl.dgoossens.chiselsandbits2.client.render.cache.CacheMap;
import nl.dgoossens.chiselsandbits2.client.render.models.BaseSmartModel;
import nl.dgoossens.chiselsandbits2.client.render.models.CacheClearable;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateInstance;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelNeighborRenderTracker;
import nl.dgoossens.chiselsandbits2.common.utils.BitUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ModelUtil;

import javax.annotation.Nonnull;
import java.util.Random;

public class ChiseledBlockSmartModel extends BaseSmartModel implements CacheClearable {
    private static final CacheMap<ItemStack, IBakedModel> itemToModel = new CacheMap<>();
    private static final CacheMap<VoxelBlobStateInstance, Integer> sideCache = new CacheMap<>();
    private static final CacheMap<ModelRenderState, ChiseledBlockBaked> modelCache = new CacheMap<>();

    public static int getSides(final ChiseledBlockTileEntity te) {
        final VoxelBlobStateReference ref = te.getVoxelReference();
        if (ref == null) return 0;

        int out = 0;
        synchronized (sideCache) {
            out = sideCache.get(ref.getInstance());
            if (out == 0) {
                final VoxelBlob blob = ref.getVoxelBlob();
                out = blob.getSideFlags(0, VoxelBlob.DIMENSION_MINUS_ONE, VoxelBlob.DIMENSION2);
                sideCache.put(ref.getInstance(), out);
            }
        }

        return out;
    }

    public static ChiseledBlockBaked getCachedModel(final ChiseledBlockTileEntity te) {
        return getCachedModel(te.getPrimaryBlock(), te.getVoxelReference(), te.getRenderTracker(), te.getRenderTracker().getRenderState(te.getVoxelReference()), getModelFormat());
    }

    private static VertexFormat getModelFormat() {
        return !ForgeMod.forgeLightPipelineEnabled ? DefaultVertexFormats.ITEM : ChiselsAndBitsBakedQuad.VERTEX_FORMAT;
    }

    private static ChiseledBlockBaked getCachedModel(final Integer primaryBlock, final VoxelBlobStateReference reference, final VoxelNeighborRenderTracker renderTracker, final ModelRenderState mrs, final VertexFormat format) {
        if (reference == null)
            return new ChiseledBlockBaked(primaryBlock, null, new ModelRenderState(), format);

        ChiseledBlockBaked out = null;
        if ((renderTracker == null || renderTracker.isValid()) && format == getModelFormat()) out = modelCache.get(mrs);
        if (out == null) {
            out = new ChiseledBlockBaked(primaryBlock, reference, mrs, format);
            if (out.isEmpty()) //Add the breaking texture the model
                out.setSprite(ModelUtil.getBreakingTexture(primaryBlock));

            if (format == getModelFormat() && mrs != null)
                modelCache.put(mrs, out);
        }

        return out;
    }

    @Override
    public IBakedModel handleBlockState(final BlockState myState, final Random rand, @Nonnull final IModelData modelData) {
        VoxelBlobStateReference data = modelData.getData(ChiseledBlockTileEntity.VOXEL_DATA);
        if (data == null)
            data = new VoxelBlobStateReference(); //Full gray block is default (this gets used in statistics menu)

        VoxelNeighborRenderTracker rTracker = modelData.getData(ChiseledBlockTileEntity.NEIGHBOUR_RENDER_TRACKER);
        int primaryBlock = modelData.hasProperty(ChiseledBlockTileEntity.PRIMARY_BLOCKSTATE) ? modelData.getData(ChiseledBlockTileEntity.PRIMARY_BLOCKSTATE) : 0;
        if (rTracker == null)
            rTracker = new VoxelNeighborRenderTracker(null, null);

        if (rTracker.isDynamic())
            return ChiseledBlockBaked.createFromTexture(ModelUtil.getBreakingTexture(primaryBlock));

        ChiseledBlockBaked baked = getCachedModel(primaryBlock, data, rTracker, rTracker.getRenderState(data), getModelFormat());
        rTracker.setFaceCount(baked.faceCount());
        return baked;
    }

    @Override
    public IBakedModel handleItemState(final IBakedModel originalModel, final ItemStack stack, final World world, final LivingEntity entity) {
        IBakedModel mdl = itemToModel.get(stack);
        if (mdl != null) return mdl;

        CompoundNBT c = stack.getTag();
        if (c == null)
            c = new CompoundNBT();
        else
            c = c.getCompound(ChiselUtil.NBT_BLOCKENTITYTAG);

        byte[] vdata = c.getByteArray(NBTBlobConverter.NBT_VERSIONED_VOXEL);
        final int blockP = c.contains(NBTBlobConverter.NBT_PRIMARY_STATE) ? c.getInt(NBTBlobConverter.NBT_PRIMARY_STATE) : VoxelBlob.AIR_BIT;
        //If the NBT defines no data, we render a gray coloured block. (this fixes things like statistics menu and /give where a NBT-less item stack is created.
        VoxelBlobStateReference state = c.contains(NBTBlobConverter.NBT_VERSIONED_VOXEL) ? new VoxelBlobStateReference(vdata) : new VoxelBlobStateReference();

        mdl = getCachedModel(blockP, state, null, null, DefaultVertexFormats.ITEM);
        itemToModel.put(stack, mdl);
        return mdl;
    }

    @Override
    public void clearCache() {
        modelCache.clear();
        sideCache.clear();
        itemToModel.clear();
    }
}
