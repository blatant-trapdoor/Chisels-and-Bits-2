package nl.dgoossens.chiselsandbits2.client.render.chiseledblock.ter;

import com.google.common.base.Stopwatch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.client.render.model.CacheType;
import nl.dgoossens.chiselsandbits2.common.util.ChiselUtil;

import javax.annotation.Nullable;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The general manager for the Chiseled Block TileEntityRenderers.
 */
public class RenderingManager {
    private final WeakHashMap<World, WorldTracker> worldTrackers = new WeakHashMap<>();
    private final ThreadPoolExecutor pool;
    private final Queue<TessellatorReferenceHolder> previousTessellators = new LinkedBlockingQueue<>();

    public RenderingManager() {
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
    }

    /**
     * Submit an old tessellator for reuse.
     */
    public void submitTessellator(final Tessellator t) {
        previousTessellators.add(new TessellatorReferenceHolder(t));
    }

    /**
     * Get a tessellator to reuse that was previously
     */
    @Nullable
    public Tessellator getRecycledTessellator() {
        Tessellator tessellator = null;
        do {
            final TessellatorReferenceHolder holder = previousTessellators.poll();
            if (holder != null) {
                tessellator = holder.get();
                if (tessellator == null)
                    holder.dispose();
            }
        }
        while (tessellator == null && !previousTessellators.isEmpty());
        return tessellator;
    }

    /**
     * Get the maximal amount of tesselators that can be active simultaneously.
     */
    public int getMaxTesselators() {
        int dynamicTess = ChiselsAndBits2.getInstance().getConfig().dynamicMaxConcurrentTesselators.get();
        if (ChiselUtil.isLowMemoryMode()) dynamicTess = Math.min(2, dynamicTess);
        return dynamicTess;
    }

    /**
     * Get the worldtracker for the world the player is in.
     */
    public WorldTracker getTracker() {
        final World world = Minecraft.getInstance().player.world;
        if (!worldTrackers.containsKey(world))
            worldTrackers.put(world, new WorldTracker());
        return worldTrackers.get(world);
    }

    /**
     * Add a task to be executed on the next frame.
     */
    public void addNextFrameTask(final Runnable r) {
        getTracker().nextFrameTasks.offer(r);
    }

    /**
     * Add a new future tracker for which rendering will be attempted to be
     * finalised every frame until it is finalised. (at which point it will be removed)
     */
    public void addFutureTracker(final RenderCache renderCache) {
        getTracker().futureTrackers.add(renderCache);
    }

    /**
     * Finalizes the rendering of a render cache. Only works if the render cache
     * has finished rendering in the background.
     */
    public boolean finalizeRendering(final RenderCache renderCache) {
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
    public void uploadVBOs() {
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
    public void uploadVBO(final UploadTracker t) {
        final Tessellator tx = t.getTessellator();
        if (t.getRenderCache().needsRebuilding())
            t.getRenderCache().setRenderState(GfxRenderState.getNewState(tx.getBuffer().getVertexCount()));

        if (t.getRenderCache().prepareRenderState(tx))
            t.submitForReuse();
    }

    /**
     * Runs all tasks in the queue one after another.
     */
    public void runJobs(final Queue<Runnable> tasks) {
        do {
            final Runnable x = tasks.poll();
            if (x == null) break;
            x.run();
        } while (true);
    }

    /**
     * Submit a task to the rendering thread.
     */
    public void submit(Runnable task) {
        pool.submit(task);
    }

    /**
     * Tracks all ongoing rendering tasks in a given world.
     */
    public static class WorldTracker {
        //Previously the futureTrackers where a linked list of FutureTracker which was a hull for the RenderCache object.
        private final HashSet<RenderCache> futureTrackers = new HashSet<>();
        private final Queue<UploadTracker> uploaders = new ConcurrentLinkedQueue<>();
        private final Queue<Runnable> nextFrameTasks = new ConcurrentLinkedQueue<>();

        /**
         * Returns true if a new future tracker can be added to this tracker.
         */
        public boolean acceptsNewTasks(RenderCache cache) {
            return futureTrackers.contains(cache) || futureTrackers.size() < ChiselsAndBits2.getInstance().getClient().getRenderingManager().getMaxTesselators();
        }
    }

    /**
     * Holds a reference to a Tessellator.
     */
    public static class TessellatorReferenceHolder {
        SoftReference<Tessellator> tesssellator;

        private TessellatorReferenceHolder(final Tessellator tesssellator) {
            this.tesssellator = new SoftReference<>(tesssellator);
        }

        private void dispose() {
            if (tesssellator != null)
                tesssellator = null;
        }

        public Tessellator get() {
            return tesssellator == null ? null : tesssellator.get();
        }

        @Override
        protected void finalize() {
            dispose();
        }
    }

    //Separate events into separate subclass to avoid forge making an instance or anything.
    @Mod.EventBusSubscriber(Dist.CLIENT)
    public static class Events {
        private static boolean lastFancy = false;

        @SubscribeEvent
        public static void nextFrame(final RenderWorldLastEvent e) {
            RenderingManager m = ChiselsAndBits2.getInstance().getClient().getRenderingManager();
            m.runJobs(m.getTracker().nextFrameTasks);
            m.uploadVBOs();

            if (Minecraft.getInstance().gameSettings.fancyGraphics != lastFancy) {
                lastFancy = Minecraft.getInstance().gameSettings.fancyGraphics;
                CacheType.DEFAULT.call();
            }
        }
    }
}
