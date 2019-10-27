package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;

/**
 * Builds what a certain face of a block should look like.
 */
//TODO improve documentation of the face builder
public interface IFaceBuilder {
    /**
     * Sets the face that this face builder is building and the tint of said face.
     */
    void setFace(Direction myFace, int tintIndex);

    /**
     * Input data into the builder at a given element.
     */
    void put(int element, float... args);

    /**
     * Starts the builder.
     */
    void begin();

    /**
     * Creates a quad with a given sprite.
     */
    BakedQuad create(TextureAtlasSprite sprite);

    /**
     * Gets this faces vertex format.
     */
    VertexFormat getFormat();
}
