package nl.dgoossens.chiselsandbits2.client.render.chiseledblock.ter;

import net.minecraft.client.renderer.Tessellator;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

public class UploadTracker {
    private final RenderCache renderCache;
    private Tessellator src;

    public UploadTracker(final RenderCache rc, final Tessellator tess) {
        this.renderCache = rc;
        this.src = tess;
    }

    public RenderCache getRenderCache() {
        return renderCache;
    }

    public Tessellator getTessellator() {
        if (src == null) throw new NullPointerException();
        return src;
    }

    public void submitForReuse() {
        ChiselsAndBits2.getInstance().getClient().getRenderingManager().submitTessellator(src);
        src = null;
    }
}
