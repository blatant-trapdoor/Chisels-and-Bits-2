package nl.dgoossens.chiselsandbits2.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.ForgeMod;
import nl.dgoossens.chiselsandbits2.client.render.cache.CacheMap;
import nl.dgoossens.chiselsandbits2.client.render.models.BaseSmartModel;
import nl.dgoossens.chiselsandbits2.client.render.models.ModelCombined;
import nl.dgoossens.chiselsandbits2.client.render.models.NullBakedModel;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateInstance;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlobStateReference;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelNeighborRenderTracker;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import javax.annotation.Nonnull;
import java.util.*;

public class ChiseledBlockSmartModel extends BaseSmartModel implements ICacheClearable {
    static final CacheMap<VoxelBlobStateReference, ChiseledBlockBaked> solidCache = new CacheMap<>();
    static final CacheMap<ItemStack, IBakedModel> itemToModel = new CacheMap<>();
    static final CacheMap<VoxelBlobStateInstance, Integer> sideCache = new CacheMap<>();

    @SuppressWarnings( "unchecked" )
    static private final Map<ModelRenderState, ChiseledBlockBaked>[] modelCache = new Map[5];

    static
    {
        final int count = ChiselLayer.values().length;

        if ( modelCache.length != count )
        {
            throw new RuntimeException( "Invalid Number of BlockRenderLayer" );
        }

        // setup layers.
        for ( final ChiselLayer l : ChiselLayer.values() )
        {
            modelCache[l.ordinal()] = Collections.synchronizedMap( new WeakHashMap<>() );
        }
    }

    public static int getSides(
            final ChiseledBlockTileEntity te )
    {
        final VoxelBlobStateReference ref = te.getVoxelReference();
        Integer out = null;

        if ( ref == null )
        {
            return 0;
        }

        synchronized ( sideCache )
        {
            out = sideCache.get( ref.getInstance() );
            if ( out == null )
            {
                final VoxelBlob blob = ref.getVoxelBlob();

                // ignore non-solid, and fluids.
                //blob.filter( BlockRenderLayer.SOLID );
                //TODO blob.filterFluids( false );

                out = blob.getSideFlags( 0, VoxelBlob.DIMENSION_MINUS_ONE, VoxelBlob.DIMENSION2);
                sideCache.put( ref.getInstance(), out );
            }
        }

        return out;
    }

    public static ChiseledBlockBaked getCachedModel(
            final ChiseledBlockTileEntity te,
            final ChiselLayer layer )
    {
        final VoxelBlobStateReference data = te.getVoxelReference();
        final VoxelNeighborRenderTracker rTracker = te.getRenderTracker();
        Integer blockP = te.getPrimaryBlock();

        blockP = blockP == null ? 0 : blockP;

        return getCachedModel( blockP, data, getRenderState( rTracker, data ), layer, getModelFormat() );
    }

    private static VertexFormat getModelFormat()
    {
        return ForgePipelineDisabled() ? DefaultVertexFormats.ITEM : ChiselsAndBitsBakedQuad.VERTEX_FORMAT;
    }

    public static boolean ForgePipelineDisabled()
    {
        return !ForgeMod.forgeLightPipelineEnabled;
    }

    private static ChiseledBlockBaked getCachedModel(
            final Integer blockP,
            final VoxelBlobStateReference data,
            final ModelRenderState mrs,
            final ChiselLayer layer,
            final VertexFormat format )
    {
        if ( data == null )
        {
            return new ChiseledBlockBaked( blockP, layer, null, new ModelRenderState( null ), format );
        }

        ChiseledBlockBaked out = null;

        if ( format == getModelFormat() )
        {
            if ( layer == ChiselLayer.SOLID )
            {
                out = solidCache.get( data );
            }
            else
            {
                out = mrs == null ? null : modelCache[layer.ordinal()].get( mrs );
            }
        }

        if ( out == null )
        {
            out = new ChiseledBlockBaked( blockP, layer, data, mrs, format );

            if ( out.isEmpty() )
            {
                out = ChiseledBlockBaked.breakingParticleModel( layer, blockP );
            }

            if ( format == getModelFormat() )
            {
                if ( layer == ChiselLayer.SOLID )
                {
                    solidCache.put( data, out );
                }
                else if ( mrs != null )
                {
                    modelCache[layer.ordinal()].put( mrs, out );
                }
            }
        }

        return out;
    }

    @Override
    public IBakedModel handleBlockState(
            final BlockState myState,
            final Random rand,
            @Nonnull final IModelData modelData)
    {
        if ( myState == null )
        {
            return NullBakedModel.instance;
        }

        // This seems silly, but it proves to be faster in practice.
        VoxelBlobStateReference data = modelData.getData(ChiseledBlockTileEntity.VOXEL_DATA);
        VoxelNeighborRenderTracker rTracker = modelData.getData(ChiseledBlockTileEntity.NEIGHBOUR_RENDER_TRACKER);
        Integer blockP = modelData.getData(ChiseledBlockTileEntity.PRIMARY_BLOCKSTATE);

        blockP = blockP == null ? 0 : blockP;

        final BlockRenderLayer layer = net.minecraftforge.client.MinecraftForgeClient.getRenderLayer();

        if ( layer == null )
        {
            final ChiseledBlockBaked[] models = new ChiseledBlockBaked[ChiselLayer.values().length];
            int o = 0;

            for ( final ChiselLayer l : ChiselLayer.values() )
            {
                models[o++] = getCachedModel( blockP, data, getRenderState( rTracker, data ), l, getModelFormat() );
            }

            return new ModelCombined( models );
        }

        if ( rTracker != null && rTracker.isDynamic() )
        {
            return ChiseledBlockBaked.breakingParticleModel( ChiselLayer.fromLayer( layer, false ), blockP );
        }

        IBakedModel baked = null;
        int faces = 0;

        if ( layer == BlockRenderLayer.SOLID )
        {
            final ChiseledBlockBaked a = getCachedModel( blockP, data, getRenderState( rTracker, data ), ChiselLayer.fromLayer( layer, false ), getModelFormat() );
            final ChiseledBlockBaked b = getCachedModel( blockP, data, getRenderState( rTracker, data ), ChiselLayer.fromLayer( layer, true ), getModelFormat() );

            faces = a.faceCount() + b.faceCount();

            if ( a.isEmpty() )
            {
                baked = b;
            }
            else if ( b.isEmpty() )
            {
                baked = a;
            }
            else
            {
                baked = new ModelCombined( a, b );
            }
        }
        else
        {
            final ChiseledBlockBaked t = getCachedModel( blockP, data, getRenderState( rTracker, data ), ChiselLayer.fromLayer( layer, false ), getModelFormat() );
            faces = t.faceCount();
            baked = t;
        }

        if ( rTracker != null )
        {
            rTracker.setAbovelimit( layer, faces );
        }

        return baked;
    }

    @Override
    public IBakedModel handleItemState(
            final IBakedModel originalModel,
            final ItemStack stack,
            final World world,
            final LivingEntity entity )
    {
        IBakedModel mdl = itemToModel.get( stack );

        if ( mdl != null )
        {
            return mdl;
        }

        CompoundNBT c = stack.getTag();
        if ( c == null )
        {
            return this;
        }

        c = c.getCompound( ModUtil.NBT_BLOCKENTITYTAG );
        if ( c == null )
        {
            return this;
        }

        byte[] vdata = c.getByteArray( NBTBlobConverter.NBT_VERSIONED_VOXEL );
        final Integer blockP = c.getInt( NBTBlobConverter.NBT_PRIMARY_STATE );

        final IBakedModel[] models = new IBakedModel[ChiselLayer.values().length];
        for ( final ChiselLayer l : ChiselLayer.values() )
        {
            models[l.ordinal()] = getCachedModel( blockP, new VoxelBlobStateReference( vdata ), null, l, DefaultVertexFormats.ITEM );
        }

        mdl = new ModelCombined( models );

        itemToModel.put( stack, mdl );

        return mdl;
    }

    @Override
    public void clearCache()
    {
        for ( final ChiselLayer l : ChiselLayer.values() )
        {
            modelCache[l.ordinal()].clear();
        }

        sideCache.clear();
        solidCache.clear();
        itemToModel.clear();
    }

    private static ModelRenderState getRenderState(
            final VoxelNeighborRenderTracker renderTracker,
            final VoxelBlobStateReference data )
    {
        if ( renderTracker != null )
        {
            return renderTracker.getRenderState( data );
        }

        return null;
    }
}
