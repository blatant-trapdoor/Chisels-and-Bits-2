package nl.dgoossens.chiselsandbits2.client.render.ter;

import net.minecraft.client.renderer.Tessellator;

public class UploadTracker {
    final RenderCache renderCache;
    private Tessellator src;

    public Tessellator getTessellator() { if(src==null) throw new NullPointerException(); return src; }
    public UploadTracker(final RenderCache rc, final Tessellator tess) {
        this.renderCache = rc; this.src = tess;
    }
    public void submitForReuse() {
        BackgroundRenderer.submitTessellator(src);
        src = null;
    }
}
