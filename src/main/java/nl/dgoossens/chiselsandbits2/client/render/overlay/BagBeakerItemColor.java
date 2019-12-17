package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

public class BagBeakerItemColor implements IItemColor {
    private int layer;
    public BagBeakerItemColor(int layer) {
        this.layer = layer;
    }

    @Override
    public int getColor(ItemStack stack, int tint) {
        if (tint != layer) return -1;
        return ChiselsAndBits2.getInstance().getItems().getColourableColour(stack);
    }
}
