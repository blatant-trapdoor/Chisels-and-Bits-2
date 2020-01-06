package nl.dgoossens.chiselsandbits2.client.render.ter;

import com.google.common.base.Stopwatch;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Region;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockBaked;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockSmartModel;
import nl.dgoossens.chiselsandbits2.client.render.models.CacheType;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.*;

public class ChiseledBlockTER extends TileEntityRenderer<ChiseledBlockTileEntity> {
    public static volatile ChiseledBlockTER INSTANCE = new ChiseledBlockTER();

    //--- RENDERING MANGEMENT METHODS ---
    private final WeakHashMap<World, WorldTracker> worldTrackers = new WeakHashMap<>();
    private final Random random = new Random();
    private int isConfigured = 0;
    private ThreadPoolExecutor pool;

    /**
     * Private initialiser only fired for the INSTANCE object.
     */
    private ChiseledBlockTER() {
        try {
            ThreadFactory threadFactory = (r) -> {
                final Thread t = new Thread(r);
                t.setPriority(Thread.NORM_PRIORITY - 1);
                t.setName("C&B2 Render Thread");
                return t;
            };

            int processors = Runtime.getRuntime().availableProcessors();
            if (ChiselUtil.isLowMemoryMode()) processors = 1;
            pool = new ThreadPoolExecutor(1, processors, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(64), threadFactory);
            pool.allowCoreThreadTimeOut(false);
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Get the maximal amount of tesselators that can be active simultaneously.
     */
    int getMaxTesselators() {
        int dynamicTess = ChiselsAndBits2.getInstance().getConfig().dynamicMaxConcurrentTesselators.get();
        if (ChiselUtil.isLowMemoryMode()) dynamicTess = Math.min(2, dynamicTess);
        return dynamicTess;
    }

    /**
     * Get the worldtracker for the world the player is in.
     */
    private WorldTracker getTracker() {
        //TODO would a mod like betterportals need this to work for all dimensions simultaneously?
        final World world = Minecraft.getInstance().player.world;
        if (!worldTrackers.containsKey(world))
            worldTrackers.put(world, new WorldTracker());
        return worldTrackers.get(world);
    }

    /**
     * Add a task to be executed on the next frame.
     */
    void addNextFrameTask(final Runnable r) {
        getTracker().nextFrameTasks.offer(r);
    }

    /**
     * Add a new future tracker for which rendering will be attempted to be
     * finalised every frame until it is finalised. (at which point it will be removed)
     */
    private void addFutureTracker(final RenderCache renderCache) {
        getTracker().futureTrackers.add(renderCache);
    }

    /**
     * Finalizes the rendering of a render cache. Only works if the render cache
     * has finished rendering in the background.
     */
    private boolean finalizeRendering(final RenderCache renderCache) {
        if (renderCache.hasRenderingCompleted()) {
            try {
                final Tessellator t = renderCache.getRenderingTask().get();
                getTracker().uploaders.offer(new UploadTracker(renderCache, t));
            } catch (CancellationException cancel) { //We're fine if the future got cancelled.
            } catch (Exception x) {
                x.printStackTrace();
            } finally {
                renderCache.finishRendering();
            }
            return true;
        }
        if (!renderCache.isRendering())
            return true; //Remove if not rendering but not completed
        return false;
    }

    /**
     * Uploads all VBOs, finalises rendering for all future trackers.
     */
    private void uploadVBOs() {
        final WorldTracker tracker = getTracker();
        tracker.futureTrackers.removeIf(this::finalizeRendering);
        final Stopwatch w = Stopwatch.createStarted();
        do { //We always upload one, no matter how many ms you want us to do it for.
            final UploadTracker t = tracker.uploaders.poll();
            if (t == null) return;
            uploadVBO(t);
        } while (w.elapsed(TimeUnit.MILLISECONDS) < ChiselsAndBits2.getInstance().getConfig().maxMillisecondsUploadingPerFrame.get());
    }

    /**
     * Uploads a given VBO.
     */
    private void uploadVBO(final UploadTracker t) {
        final Tessellator tx = t.getTessellator();
        if (t.renderCache.needsRebuilding())
            t.renderCache.setRenderState(GfxRenderState.getNewState(tx.getBuffer().getVertexCount()));

        if (t.renderCache.prepareRenderState(tx))
            t.submitForReuse();
    }

    /**
     * Runs all tasks in the queue one after another.
     */
    private void runJobs(final Queue<Runnable> tasks) {
        do {
            final Runnable x = tasks.poll();
            if (x == null) break;
            x.run();
        } while (true);
    }

    //---- RENDERING LOGIC ---

    @Override
    public void renderTileEntityFast(final ChiseledBlockTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final BufferBuilder buffer) {
        renderLogic(te, x, y, z, partialTicks, destroyStage);
    }

    @Override
    public void render(final ChiseledBlockTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        renderLogic(te, x, y, z, partialTicks, destroyStage);
    }

    private void renderLogic(final ChiseledBlockTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        if (destroyStage >= 0) {
            renderBreakingEffects(te, x, y, z, partialTicks, destroyStage);
            return;
        }
        final TileChunk rc = te.getChunk(te.getWorld());
        final BlockPos chunkOffset = te.getChunk(te.getWorld()).chunkOffset();

        if(!rc.needsRebuilding() && te.getRenderTracker().isInvalid(te.getWorld(), te.getPos()))
            rc.rebuild();

        //Rebuild if necessary
        if (rc.needsRebuilding() && getTracker().acceptsNewTasks(rc)) {
            try {
                final Region cache = new Region(getWorld(), chunkOffset, chunkOffset.add(TileChunk.TILE_CHUNK_SIZE, TileChunk.TILE_CHUNK_SIZE, TileChunk.TILE_CHUNK_SIZE));
                final FutureTask<Tessellator> newFuture = new FutureTask<>(new BackgroundRenderer(cache, chunkOffset, te.getChunk(te.getWorld()).getTileList()));
                rc.setRenderingTask(newFuture);

                pool.submit(newFuture);
                addFutureTracker(rc);
            } catch (RejectedExecutionException err) {
                err.printStackTrace();
            }
        }

        final GfxRenderState dl = rc.getRenderState();
        if (dl != null && dl.shouldRender()) {
            if (!dl.validForUse()) {
                rc.setRenderState(null);
                return;
            }

            GL11.glPushMatrix();
            GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX + chunkOffset.getX(),
                    -TileEntityRendererDispatcher.staticPlayerY + chunkOffset.getY(),
                    -TileEntityRendererDispatcher.staticPlayerZ + chunkOffset.getZ());

            configureGLState();
            dl.render();
            unconfigureGLState();
            GL11.glPopMatrix();
        }
    }

    private void configureGLState() {
        isConfigured++;
        if (isConfigured == 1) {
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 0, 0); //lightmapTexUnit

            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

            RenderHelper.disableStandardItemLighting();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);

            GlStateManager.enableBlend();
            GlStateManager.disableAlphaTest();

            GlStateManager.enableCull();
            GlStateManager.enableTexture();

            if (Minecraft.isAmbientOcclusionEnabled())
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
            else
                GlStateManager.shadeModel(GL11.GL_FLAT);
        }
    }

    private void unconfigureGLState() {
        isConfigured--;
        if (isConfigured > 0) return;

        GlStateManager.clearCurrentColor();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableBlend();

        RenderHelper.enableStandardItemLighting();
    }

    private void renderBreakingEffects(final ChiseledBlockTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        final String file = DESTROY_STAGES[destroyStage].toString().replace("textures/", "").replace(".png", "");
        final TextureAtlasSprite damageTexture = Minecraft.getInstance().getTextureMap().getAtlasSprite(file);

        GlStateManager.pushMatrix();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        final BlockPos cp = te.getPos();
        GlStateManager.translated(x - cp.getX(), y - cp.getY(), z - cp.getZ());

        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(0, 0, 0);

        final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
        final BlockState estate = te.getBlockState();

        final ChiseledBlockBaked model = ChiseledBlockSmartModel.getCachedModel(te);

        if (!model.isEmpty()) {
            final IBakedModel damageModel = new SimpleBakedModel.Builder(estate, model, damageTexture, random, random.nextLong())
                    .setTexture(damageTexture) //Just to avoid the RuntimeException, we don't use this.
                    .build();
            blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), damageModel, estate, cp, buffer, true, random, random.nextLong());
        }

        tessellator.draw();
        buffer.setTranslation(0.0D, 0.0D, 0.0D);

        GlStateManager.clearCurrentColor();
        GlStateManager.popMatrix();
    }

    private static class WorldTracker {
        //Previously the futureTrackers where a linked list of FutureTracker which was a hull for the RenderCache object.
        private final HashSet<RenderCache> futureTrackers = new HashSet<>();
        private final Queue<UploadTracker> uploaders = new ConcurrentLinkedQueue<>();
        private final Queue<Runnable> nextFrameTasks = new ConcurrentLinkedQueue<>();

        /**
         * Returns true if a new future tracker can be added to this tracker.
         */
        public boolean acceptsNewTasks(RenderCache cache) {
            return futureTrackers.contains(cache) || futureTrackers.size() < ChiseledBlockTER.INSTANCE.getMaxTesselators();
        }
    }

    //Separate events into separate subclass to avoid forge making an instance or anything.
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class Events {
        private static boolean lastFancy = false;

        @SubscribeEvent
        public static void nextFrame(final RenderWorldLastEvent e) {
            INSTANCE.runJobs(INSTANCE.getTracker().nextFrameTasks);
            INSTANCE.uploadVBOs();

            //TODO find out why the lastFancy stuff is necessary
            if (Minecraft.getInstance().gameSettings.fancyGraphics != lastFancy) {
                lastFancy = Minecraft.getInstance().gameSettings.fancyGraphics;
                CacheType.DEFAULT.call();
            }
        }
    }
}
