package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.client.renderer.Tessellator;

import java.util.*;
import java.util.concurrent.FutureTask;

/**
 * The cache of one region worth of rendering.
 * A region of 8x8x8 is stored in one render cache.
 */
public class RenderCache {
    private final UUID uniqueId = UUID.randomUUID();
    private FutureTask<Tessellator> renderTask = null;
    private GfxRenderState vboRenderer = null; //if this is null the RenderCache is new.
    private GfxRenderState oldRenderer = null; //To use if the vboRenderer isn't ready yet.

    /**
     * Returns whether or not this cache needs to be re-rendered.
     */
    public boolean needsRebuilding() {
        return vboRenderer == null && !isRendering();
    }

    /**
     * Returns the rendering state to render.
     */
    public GfxRenderState getRenderState() {
        return vboRenderer == null ? oldRenderer : vboRenderer;
    }

    /**
     * Sets the new render state.
     */
    public void setRenderState(GfxRenderState state) {
        if(vboRenderer == null && state == null) return; //Protect the oldRenderer if it's taking some time to get the new one.
        oldRenderer = vboRenderer;
        vboRenderer = state;
    }

    /**
     * Prepares the rendering state.
     */
    public boolean prepareRenderState(Tessellator tx) {
        if(vboRenderer == null) return false;
        vboRenderer = vboRenderer.prepare(tx);
        return true;
    }

    /**
     * Returns whether there is currently an active rendering task.
     */
    public boolean isRendering() {
        return renderTask != null;
    }

    /**
     * Returns if the active rendering task has been completed.
     */
    public boolean hasRenderingCompleted() {
        return renderTask != null && renderTask.isDone();
    }

    /**
     * Gets the active rendering task.
     */
    public FutureTask<Tessellator> getRenderingTask() {
        return renderTask;
    }

    /**
     * Cancels the current ongoing rendering task.
     */
    public void cancelTask() {
        if(renderTask != null && !renderTask.isDone())
            renderTask.cancel(true);
    }

    /**
     * Marks the rendering as done and resets the rendering task.
     */
    public void finishRendering() {
        //TODO System.out.println("[FINISH] Finished rendering render cache...");
        renderTask = null;
    }

    /**
     * Sets the current rendering task.
     */
    public void setRenderingTask(FutureTask<Tessellator> f) {
        if(f == null) throw new IllegalArgumentException("Can't set new rendering task to null, use RenderCache#finishRendering instead.");
        renderTask = f;
    }

    /**
     * Rebuild can be called when this tile needs to be re-rendered.
     * This will cause a currently active rendering task to aborted immediately.
     */
    public void rebuild() {
        //TODO System.out.println("[REBUILD] Asking for rebuild!");
        setRenderState(null);
        if (renderTask != null) renderTask.cancel(true);
        renderTask = null;
    }

    /**
     * Get the unique id attached to this render cache.
     * This is used to ensure a single render cache can't have two tasks
     * pending.
     */
    public UUID getUniqueId() {
        return uniqueId;
    }
}
