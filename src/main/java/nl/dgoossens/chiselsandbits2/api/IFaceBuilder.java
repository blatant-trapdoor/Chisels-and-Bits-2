package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;

//TODO This interface needs more documentation!
public interface IFaceBuilder {
    void setFace(Direction myFace, int tintIndex);

    void put(int element, float... args);

    void begin();

    BakedQuad create(TextureAtlasSprite sprite);

    VertexFormat getFormat();
}
