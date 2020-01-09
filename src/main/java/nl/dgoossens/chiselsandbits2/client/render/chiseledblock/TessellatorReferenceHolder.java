package nl.dgoossens.chiselsandbits2.client.render.chiseledblock;

import net.minecraft.client.renderer.Tessellator;

import java.lang.ref.SoftReference;

/**
 * Holds a reference to a Tessellator.
 */
public class TessellatorReferenceHolder {
    private SoftReference<Tessellator> tesssellator;

    public TessellatorReferenceHolder(final Tessellator tesssellator) {
        this.tesssellator = new SoftReference<>(tesssellator);
    }

    public void dispose() {
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
