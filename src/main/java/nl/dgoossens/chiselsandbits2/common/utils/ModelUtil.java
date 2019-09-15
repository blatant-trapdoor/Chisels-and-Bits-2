package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.ChiselLayer;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockBaked;
import nl.dgoossens.chiselsandbits2.client.render.ICacheClearable;
import nl.dgoossens.chiselsandbits2.client.render.models.helpers.ModelQuadLayer;
import nl.dgoossens.chiselsandbits2.client.render.models.helpers.ModelUVAverager;
import nl.dgoossens.chiselsandbits2.client.render.models.helpers.ModelVertexRange;

import java.lang.reflect.Field;
import java.util.*;

public class ModelUtil implements ICacheClearable {
    private final static HashMap<Integer, ResourceLocation> blockToTexture[];
    private static HashMap<Integer, ModelQuadLayer[]> cache = new HashMap<>();
    private static HashMap<Integer, ChiseledBlockBaked> breakCache = new HashMap<>();

    static
    {
        blockToTexture = new HashMap[Direction.values().length * BlockRenderLayer.values().length];

        for ( int x = 0; x < blockToTexture.length; x++ )
        {
            blockToTexture[x] = new HashMap<>();
        }
    }

    @Override
    public void clearCache()
    {
        for ( int x = 0; x < blockToTexture.length; x++ )
        {
            blockToTexture[x].clear();
        }

        cache.clear();
        breakCache.clear();
    }

    public static boolean isOne(
            final float v) {
        return Math.abs(v) < 0.01;
    }

    public static boolean isZero(
            final float v) {
        return Math.abs(v - 1.0f) < 0.01;
    }

    public static ModelQuadLayer[] getCachedFace(
            final int stateID,
            final Random weight,
            final Direction face,
            final BlockRenderLayer layer) {
        if(layer == null) {
            return null;
        }

        final int cacheVal = stateID << 6 | layer.ordinal() << 4 | face.ordinal();

        final ModelQuadLayer[] mpc = cache.get(cacheVal);
        if(mpc != null) {
            return mpc;
        }

        final BlockRenderLayer original = net.minecraftforge.client.MinecraftForgeClient.getRenderLayer();
        try {
            ForgeHooksClient.setRenderLayer(layer);
            return getInnerCachedFace(cacheVal, stateID, weight, face, layer);
        } finally {
            // restore previous layer.
            ForgeHooksClient.setRenderLayer(original);
        }
    }

    public static IBakedModel solveModel(
            final BlockState state,
            final Random weight,
            final IBakedModel originalModel,
            final BlockRenderLayer layer )
    {
        boolean hasFaces = false;

        try
        {
            hasFaces = hasFaces( originalModel, state, null, weight );

            for ( final Direction f : Direction.values() )
            {
                hasFaces = hasFaces || hasFaces( originalModel, state, f, weight );
            }
        }
        catch ( final Exception e )
        {
            // an exception was thrown.. use the item model and hope...
            hasFaces = false;
        }

        if ( !hasFaces )
        {
            // if the model is empty then lets grab an item and try that...
            /*final ItemStack is = ModUtil.getItemFromBlock( state );
            if ( !ModUtil.isEmpty( is ) )
            {
                final IBakedModel itemModel = Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides( is, Minecraft.getInstance().world, Minecraft.getInstance().player );

                try
                {
                    hasFaces = hasFaces( originalModel, state, null, weight );

                    for ( final Direction f : Direction.values() )
                    {
                        hasFaces = hasFaces || hasFaces( originalModel, state, f, weight );
                    }
                }
                catch ( final Exception e )
                {
                    // an exception was thrown.. use the item model and hope...
                    hasFaces = false;
                }

                if ( hasFaces )
                {
                    return itemModel;
                }
                else
                {
                    return new SimpleGeneratedModel( findTexture( Block.getStateId( state ), originalModel, Direction.UP, layer ) );
                }
            }*/
        }

        return originalModel;
    }

    private static boolean hasFaces(
            final IBakedModel model,
            final BlockState state,
            final Direction f,
            final Random weight )
    {
        final List<BakedQuad> l = getModelQuads( model, state, f, weight );
        if ( l == null || l.isEmpty() )
        {
            return false;
        }

        TextureAtlasSprite texture = null;

        try
        {
            texture = findTexture( null, l, f );
        }
        catch ( final Exception e )
        {
        }

        final ModelVertexRange mvr = new ModelVertexRange();

        for ( final BakedQuad q : l )
        {
            q.pipe( mvr );
        }

        return mvr.getLargestRange() > 0 && !isMissing( texture );
    }
    
    private static ModelQuadLayer[] getInnerCachedFace(
            final int cacheVal,
            final int stateID,
            final Random weight,
            final Direction face,
            final BlockRenderLayer layer) {
        final BlockState state = ModUtil.getBlockState(stateID);
        final IBakedModel model = ModelUtil.solveModel(state, weight, Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(state), layer);
        final int lv = state.getLightValue();

        /*final Fluid fluid = BlockBitInfo.getFluidFromBlock(state.getBlock());
        if(fluid != null) {
            for(final Direction xf : Direction.VALUES) {
                final ModelQuadLayer[] mp = new ModelQuadLayer[1];
                mp[0] = new ModelQuadLayer();
                mp[0].color = fluid.getColor();
                mp[0].light = lv;

                final float V = 0.5f;
                final float Uf = 1.0f;
                final float U = 0.5f;
                final float Vf = 1.0f;

                if(xf.getAxis() == Axis.Y) {
                    mp[0].sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
                    mp[0].uvs = new float[]{Uf, Vf, 0, Vf, Uf, 0, 0, 0};
                } else if(xf.getAxis() == Axis.X) {
                    mp[0].sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getFlowing().toString());
                    mp[0].uvs = new float[]{U, 0, U, V, 0, 0, 0, V};
                } else {
                    mp[0].sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getFlowing().toString());
                    mp[0].uvs = new float[]{U, 0, 0, 0, U, V, 0, V};
                }

                mp[0].tint = 0;

                final int cacheV = stateID << 6 | layer.ordinal() << 4 | xf.ordinal();
                cache.put(cacheV, mp);
            }

            return cache.fromName(cacheVal);
        }*/

        final HashMap<Direction, ArrayList<ModelQuadLayer.ModelQuadLayerBuilder>> tmp = new HashMap<>();
        final int color = 0xffffff; //TODO BlockBitInfo.getColorFor(state, 0);

        for(final Direction f : Direction.values()) {
            tmp.put(f, new ArrayList<>());
        }

        if(model != null) {
            for(final Direction f : Direction.values()) {
                final List<BakedQuad> quads = ModelUtil.getModelQuads(model, state, f, new Random());
                processFaces(tmp, quads, state);
            }

            processFaces(tmp, ModelUtil.getModelQuads(model, state, null, new Random()), state);
        }

        for(final Direction f : Direction.values()) {
            final int cacheV = stateID << 6 | layer.ordinal() << 4 | f.ordinal();
            final ArrayList<ModelQuadLayer.ModelQuadLayerBuilder> x = tmp.get(f);
            final ModelQuadLayer[] mp = new ModelQuadLayer[x.size()];

            for(int z = 0; z < x.size(); z++) {
                mp[z] = x.get(z).build(stateID, color, lv);
            }

            cache.put(cacheV, mp);
        }

        return cache.get(cacheVal);
    }

    public static TextureAtlasSprite findQuadTexture(
            final BakedQuad q,
            final BlockState state) throws IllegalArgumentException, NullPointerException {
        final AtlasTexture map = Minecraft.getInstance().getTextureMap();
        Map<ResourceLocation, TextureAtlasSprite> mapRegisteredSprites;
        /*
                    REFLECTION
                    getting field "mapUploadedSprites"
                    in "net.minecraft.client.renderer.texture.AtlasTexture"
         */
        try {
            Field f = map.getClass().getDeclaredField("mapUploadedSprites");
            f.setAccessible(true);
            mapRegisteredSprites = (Map<ResourceLocation, TextureAtlasSprite>) f.get(map);
        } catch(Exception x) {
            x.printStackTrace();
            throw new RuntimeException("Unable to lookup textures.");
        }

        if(mapRegisteredSprites == null) {
            throw new RuntimeException("Unable to lookup textures.");
        }

        final ModelUVAverager av = new ModelUVAverager();
        q.pipe(av);

        final float U = av.getU();
        final float V = av.getV();

        final Iterator<?> iterator1 = mapRegisteredSprites.values().iterator();
        while(iterator1.hasNext()) {
            final TextureAtlasSprite sprite = (TextureAtlasSprite) iterator1.next();
            if(sprite.getMinU() <= U && U <= sprite.getMaxU() && sprite.getMinV() <= V && V <= sprite.getMaxV()) {
                return sprite;
            }
        }

        TextureAtlasSprite texture = null;

        try {
            if(q.getSprite() != null) {
                texture = q.getSprite();
            }
        } catch(final Exception e) {
        }

        if(isMissing(texture) && state != null) {
            try {
                texture = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
            } catch(final Exception err) {
            }
        }

        if(texture == null) {
            return ChiselsAndBits2.getClient().getMissingIcon();
        }

        return texture;
    }

    public static TextureAtlasSprite findTexture(
            final int BlockRef,
            final IBakedModel model,
            final Direction myFace,
            final BlockRenderLayer layer) {
        final int blockToWork = layer.ordinal() * Direction.values().length + myFace.ordinal();

        //TODO textures aren't stored as TextureAtlasSprite anymore so it can't be returned here...

        // didn't work? ok lets try scanning for the texture in the
        /*if ( blockToTexture[blockToWork].containsKey( BlockRef ) )
        {
            final ResourceLocation textureName = blockToTexture[blockToWork].fromName( BlockRef );
            return Minecraft.getInstance().getTextureManager().getTexture( textureName );
        }*/

        TextureAtlasSprite texture = null;
        final BlockState state = ModUtil.getBlockState(BlockRef);

        if(model != null) {
            try {
                texture = findTexture(texture, getModelQuads(model, state, myFace, new Random()), myFace);

                if(texture == null) {
                    for(final Direction side : Direction.values()) {
                        texture = findTexture(texture, getModelQuads(model, state, side, new Random()), side);
                    }

                    texture = findTexture(texture, getModelQuads(model, state, null, new Random()), null);
                }
            } catch(final Exception errr) {
            }
        }

        // who knows if that worked.. now lets try to fromName a texture...
        if(isMissing(texture)) {
            try {
                if(model != null) {
                    texture = model.getParticleTexture();
                }
            } catch(final Exception err) {
            }
        }

        if(isMissing(texture)) {
            try {
                texture = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);
            } catch(final Exception err) {
            }
        }

        if(texture == null) {
            texture = ChiselsAndBits2.getClient().getMissingIcon();
        }

        blockToTexture[blockToWork].put(BlockRef, texture.getName());
        return texture;
    }

    private static boolean isMissing(
            final TextureAtlasSprite texture) {
        return texture == null || texture == ChiselsAndBits2.getClient().getMissingIcon();
    }

    private static TextureAtlasSprite findTexture(
            TextureAtlasSprite texture,
            final List<BakedQuad> faceQuads,
            final Direction myFace) throws IllegalArgumentException, NullPointerException {
        for(final BakedQuad q : faceQuads) {
            if(q.getFace() == myFace) {
                texture = findQuadTexture(q, null);
            }
        }

        return texture;
    }

    public static ChiseledBlockBaked getBreakingModel(
            ChiselLayer layer,
            Integer blockStateID) {
        int key = layer.layer.ordinal() + (blockStateID << 2);
        ChiseledBlockBaked out = breakCache.get(key);

        if(out == null) {
            final BlockState state = ModUtil.getBlockState(blockStateID);
            final IBakedModel model = ModelUtil.solveModel(state, new Random(), Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(ModUtil.getBlockState(blockStateID)), layer.layer);

            if(model != null) {
                out = ChiseledBlockBaked.createFromTexture(ModelUtil.findTexture(blockStateID, model, Direction.UP, layer.layer), layer);
            } else {
                out = ChiseledBlockBaked.createFromTexture(null, null);
            }

            breakCache.put(key, out);
        }

        return out;
    }

    private static void processFaces(
            final HashMap<Direction, ArrayList<ModelQuadLayer.ModelQuadLayerBuilder>> tmp,
            final List<BakedQuad> quads,
            final BlockState state )
    {
        for ( final BakedQuad q : quads )
        {
            final Direction face = q.getFace();

            if ( face == null )
            {
                continue;
            }

            try
            {
                final TextureAtlasSprite sprite = findQuadTexture( q, state );
                final ArrayList<ModelQuadLayer.ModelQuadLayerBuilder> l = tmp.get( face );

                ModelQuadLayer.ModelQuadLayerBuilder b = null;
                for ( final ModelQuadLayer.ModelQuadLayerBuilder lx : l )
                {
                    if ( lx.cache.sprite == sprite )
                    {
                        b = lx;
                        break;
                    }
                }

                if ( b == null )
                {
                    // top/bottom
                    int uCoord = 0;
                    int vCoord = 2;

                    switch ( face )
                    {
                        case NORTH:
                        case SOUTH:
                            uCoord = 0;
                            vCoord = 1;
                            break;
                        case EAST:
                        case WEST:
                            uCoord = 1;
                            vCoord = 2;
                            break;
                        default:
                    }

                    b = new ModelQuadLayer.ModelQuadLayerBuilder( sprite, uCoord, vCoord );
                    b.cache.tint = q.getTintIndex();
                    l.add( b );
                }

                q.pipe( b.uvr );
                b.lv.setVertexFormat( q.getFormat() );
                q.pipe( b.lv );
            }
            catch ( final Exception e )
            {

            }
        }
    }

    private static List<BakedQuad> getModelQuads(
            final IBakedModel model,
            final BlockState state,
            final Direction f,
            final Random rand )
    {
        try
        {
            // try to fromName block model...
            return model.getQuads( state, f, rand );
        }
        catch ( final Throwable t )
        {

        }

        try
        {
            // try to fromName item model?
            return model.getQuads( null, f, rand );
        }
        catch ( final Throwable t )
        {

        }

        //TODO fix
        /*final ItemStack is = ModUtil.getItemFromBlock( state );
        if ( !ModUtil.isEmpty( is ) )
        {
            final IBakedModel secondModel = getOverrides( model ).handleItemState( model, is, Minecraft.getInstance().world, Minecraft.getInstance().player );

            if ( secondModel != null )
            {
                try
                {
                    return secondModel.getQuads( null, f, rand );
                }
                catch ( final Throwable t )
                {

                }
            }
        }*/

        // try to not crash...
        return Collections.emptyList();
    }
}
