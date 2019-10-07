package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.client.renderer.Tessellator;

import java.util.concurrent.FutureTask;

/**
 * The cache of one chunk worth of rendering.
 * An entire sub chunk (16x16x16) is stored in the Tessellator!
 */
public class RenderCache {
    public FutureTask<Tessellator> future = null;
    private GfxRenderState vboRenderer = null; //if this is null the RenderCache is new.
    private GfxRenderState oldRenderer = null; //To use if the vboRenderer isn't ready yet.

    /**
     * Returns whether or not this cache needs to be re-rendered.
     */
    public boolean needsRebuilding() {
        return vboRenderer == null;
    }

    /**
     * Returns the rendering state to render.
     */
    public GfxRenderState getRenderState() {
        return needsRebuilding() ? oldRenderer : vboRenderer;
    }

    /**
     * Sets the new render state.
     */
    public void setRenderState(GfxRenderState state) {
        if(needsRebuilding() && state == null) return; //Protect the oldRenderer if it's taking some time to get the new one.
        oldRenderer = vboRenderer;
        vboRenderer = state;
    }

    /**
     * Prepares the rendering state.
     * @throws NullPointerException If {@link #needsRebuilding()} returns true.
     */
    public void prepareRenderState(Tessellator tx) {
        if(vboRenderer == null) throw new NullPointerException("Can't prepare when we still need to render!");
        vboRenderer = vboRenderer.prepare(tx);
    }

    /**
     * Rebuild can be called when this tile needs to be re-rendered.
     * This will destroy the current renderer and it's data.
     */
    public void rebuild() {
        setRenderState(null);
        if (future != null) future.cancel(true);
        future = null;
    }
}
