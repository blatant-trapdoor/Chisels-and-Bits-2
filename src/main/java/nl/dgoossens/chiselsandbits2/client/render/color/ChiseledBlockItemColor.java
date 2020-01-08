package nl.dgoossens.chiselsandbits2.client.render.color;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class ChiseledBlockItemColor extends ChiseledTintColor implements IItemColor {
    @Override
    public int getColor(ItemStack item, int tint) {
        return getColor(tint);
    }
}
