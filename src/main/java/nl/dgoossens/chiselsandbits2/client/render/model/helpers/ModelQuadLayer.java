package nl.dgoossens.chiselsandbits2.client.render.model.helpers;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ModelQuadLayer {
    public float[] uvs;
    public TextureAtlasSprite sprite;
    public int light;
    public int tint;

    public static class Builder {
        private final ModelLightMapReader lv;
        private final TextureAtlasSprite sprite;
        private ModelUVReader uvr;
        private boolean hasTint;

        public ModelUVReader getUVReader() {
            return uvr;
        }

        public ModelLightMapReader getLightMapReader() {
            return lv;
        }

        public TextureAtlasSprite getSprite() {
            return sprite;
        }

        public void hasTint() {
            hasTint = true;
        }

        public Builder(final TextureAtlasSprite sprite, final int uCoord, final int vCoord) {
            this.sprite = sprite;
            lv = new ModelLightMapReader();
            uvr = new ModelUVReader(sprite, uCoord, vCoord);
        }

        public ModelQuadLayer build(final int state, final int lightValue) {
            ModelQuadLayer cache = new ModelQuadLayer();
            cache.sprite = sprite;
            cache.light = Math.max(lightValue, lv.getLightValue());
            cache.uvs = uvr.getQuadUVs();
            cache.tint = hasTint ? state : -1;
            return cache;
        }
    }
}
