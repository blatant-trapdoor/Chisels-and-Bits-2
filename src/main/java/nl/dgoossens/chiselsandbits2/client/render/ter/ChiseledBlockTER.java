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
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockBaked;
import nl.dgoossens.chiselsandbits2.client.render.ChiseledBlockSmartModel;
import nl.dgoossens.chiselsandbits2.client.render.models.CacheType;
import nl.dgoossens.chiselsandbits2.common.blocks.ChiseledBlockTileEntity;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
@OnlyIn(Dist.CLIENT)
public class ChiseledBlockTER extends TileEntityRenderer<ChiseledBlockTileEntity> {
    //--- STATIC PARTS ---
    public final static AtomicInteger pendingTess = new AtomicInteger(0);
    public final static AtomicInteger activeTess = new AtomicInteger(0);
    private final static Random RAND = new Random();
    private static final WeakHashMap<World, WorldTracker> worldTrackers = new WeakHashMap<>();
    private static ThreadPoolExecutor pool;
    private static boolean lastFancy = false;
    int isConfigured = 0;

    public ChiseledBlockTER() { //Only one instance is ever initialised
        if (pool != null) throw new UnsupportedOperationException("ChiseledBlockTER was initialised a second time!");
        final ThreadFactory threadFactory = (r) -> {
            final Thread t = new Thread(r);
            t.setPriority(Thread.NORM_PRIORITY - 1);
            t.setName("C&B Dynamic Render Thread");
            return t;
        };

        int processors = Runtime.getRuntime().availableProcessors();
        if (ModUtil.isLowMemoryMode()) processors = 1;
        pool = new ThreadPoolExecutor(1, processors, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(64), threadFactory);
        pool.allowCoreThreadTimeOut(false);
    }

    protected static int getMaxTessalators() {
        int dynamicTess = ChiselsAndBits2.getInstance().getConfig().dynamicMaxConcurrentTessalators.get();
        if (ModUtil.isLowMemoryMode()) dynamicTess = Math.min(2, dynamicTess);
        return dynamicTess;
    }

    private static WorldTracker getTracker() {
        final World world = Minecraft.getInstance().player.world;
        if (!worldTrackers.containsKey(world))
            worldTrackers.put(world, new WorldTracker());
        return worldTrackers.get(world);
    }

    protected static void addNextFrameTask(final Runnable r) {
        getTracker().nextFrameTasks.offer(r);
    }

    private static void addFutureTracker(final RenderCache renderCache) {
        getTracker().futureTrackers.add(renderCache);
    }

    private static boolean handleFutureTracker(final RenderCache renderCache) {
        if (renderCache.hasFuture() && renderCache.getFuture().isDone()) {
            try {
                final Tessellator t = renderCache.getFuture().get();
                getTracker().uploaders.offer(new UploadTracker(renderCache, t));
            } catch (CancellationException cancel) { //We're fine if the future got cancelled.
            } catch (Exception x) {
                x.printStackTrace();
            } finally {
                renderCache.resetFuture();
            }
            pendingTess.decrementAndGet();
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void nextFrame(final RenderWorldLastEvent e) {
        runJobs(getTracker().nextFrameTasks);
        uploadVBOs();

        if (Minecraft.getInstance().gameSettings.fancyGraphics != lastFancy) {
            lastFancy = Minecraft.getInstance().gameSettings.fancyGraphics;
            CacheType.DEFAULT.call();
            Minecraft.getInstance().worldRenderer.loadRenderers();
        }
    }

    private static void uploadVBOs() {
        final WorldTracker tracker = getTracker();
        tracker.futureTrackers.removeIf(ChiseledBlockTER::handleFutureTracker);
        final Stopwatch w = Stopwatch.createStarted();
        do { //We always upload one, no matter how many ms you want us to do it for.
            final UploadTracker t = tracker.uploaders.poll();
            if (t == null) return;
            uploadVBO(t);
        } while (w.elapsed(TimeUnit.MILLISECONDS) < ChiselsAndBits2.getInstance().getConfig().maxMillisecondsUploadingPerFrame.get());
    }

    private static void uploadVBO(final UploadTracker t) {
        final Tessellator tx = t.getTessellator();
        if (t.renderCache.needsRebuilding())
            t.renderCache.setRenderState(GfxRenderState.getNewState(tx.getBuffer().getVertexCount()));

        t.renderCache.prepareRenderState(tx);
        t.submitForReuse();
    }

    private static void runJobs(final Queue<Runnable> tasks) {
        do {
            final Runnable x = tasks.poll();
            if (x == null) break;
            x.run();
        } while (true);
    }

    @Override
    public void renderTileEntityFast(final ChiseledBlockTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage, final BufferBuilder buffer) {
        renderLogic(te, x, y, z, partialTicks, destroyStage);
    }

    @Override
    public void render(final ChiseledBlockTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        renderLogic(te, x, y, z, partialTicks, destroyStage);
    }

    void renderLogic(final ChiseledBlockTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
        if (destroyStage >= 0) {
            renderBreakingEffects(te, x, y, z, partialTicks, destroyStage);
            return;
        }
        final RenderCache rc = te.getChunk(te.getWorld());
        final BlockPos chunkOffset = te.getChunk(te.getWorld()).chunkOffset();
        boolean hasSubmitted = false;

        if (rc.needsRebuilding()) {
            //Rebuild!
            final int dynamicTess = getMaxTessalators();
            if (pendingTess.get() < dynamicTess && !rc.hasFuture()) {
                try {
                    final Region cache = new Region(getWorld(), chunkOffset, chunkOffset.add(16, 16, 16));
                    final FutureTask<Tessellator> newFuture = new FutureTask<>(new BackgroundRenderer(cache, chunkOffset, te.getChunk(te.getWorld()).getTileList()));
                    pool.submit(newFuture);
                    hasSubmitted = true;

                    rc.rebuild();
                    rc.setFuture(newFuture);
                    pendingTess.incrementAndGet();
                    addFutureTracker(rc);
                } catch (RejectedExecutionException err) {
                    err.printStackTrace();
                    // Yar... ??
                }
            }
        }

        //If we've scheduled the rendering this tick but this block is new. Instant render!
        //This avoids blinking blocks because background rendering doesn't get a model ready the first tick.
        if (rc.hasFuture() && hasSubmitted && rc.isNew()) {
            try {
                final Tessellator tess = rc.getFuture().get(100, TimeUnit.MILLISECONDS);
                rc.resetFuture();
                pendingTess.decrementAndGet();

                uploadVBO(new UploadTracker(rc, tess));
            } catch(TimeoutException timeout) {
                addFutureTracker(rc);
            } catch(Exception ex) {
                rc.resetFuture();
            }
        }

        //Only check this if this isn't new.
        if(!rc.isNew() && !rc.needsRebuilding()) {
            //Let the render tracker check if no neighbours changed somehow.
            te.getRenderTracker().validate(te.getWorld(), te.getPos());
            if(!te.getRenderTracker().isValid())
                te.invalidate();
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

    void renderBreakingEffects(final ChiseledBlockTileEntity te, final double x, final double y, final double z, final float partialTicks, final int destroyStage) {
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
            final IBakedModel damageModel = new SimpleBakedModel.Builder(estate, model, damageTexture, RAND, RAND.nextLong()).build();
            blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), damageModel, estate, cp, buffer, true, RAND, RAND.nextLong());
        }

        tessellator.draw();
        buffer.setTranslation(0.0D, 0.0D, 0.0D);

        GlStateManager.clearCurrentColor();
        GlStateManager.popMatrix();
    }

    private static class WorldTracker {
        //Previously the futureTrackers where a linked list of FutureTracker which was a hull for the RenderCache object.
        private final LinkedList<RenderCache> futureTrackers = new LinkedList<>();
        private final Queue<UploadTracker> uploaders = new ConcurrentLinkedQueue<>();
        private final Queue<Runnable> nextFrameTasks = new ConcurrentLinkedQueue<>();
    }
}
