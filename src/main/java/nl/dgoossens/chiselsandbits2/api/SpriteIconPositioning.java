package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * A representation of a sprite and the offsets where
 * the sprite is placed in the atlas.
 */
public class SpriteIconPositioning {
	public TextureAtlasSprite sprite;

	public double left;
	public double top;
	public double width;
	public double height;

	public SpriteIconPositioning() {}
	public SpriteIconPositioning(TextureAtlasSprite sprite) { this.sprite=sprite; }
}
