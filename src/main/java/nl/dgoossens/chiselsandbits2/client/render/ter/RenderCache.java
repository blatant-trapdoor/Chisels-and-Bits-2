package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.client.renderer.Tessellator;

import java.util.concurrent.FutureTask;

/**
 * The cache of one chunk worth of rendering.
 * An entire sub chunk (16x16x16) is stored in the Tessellator!
 */
public class RenderCache {
    public FutureTask<Tessellator> future = null;
    public GfxRenderState vboRenderer = null; //if this is null the RenderCache is new.

    /**
     * Rebuild can be called when this tile needs to be re-rendered.
     * This will destroy the current renderer and it's data.
     */
    public void rebuild() {
        vboRenderer = null;
        if (future != null) future.cancel(true);
        future = null;
    }
}
