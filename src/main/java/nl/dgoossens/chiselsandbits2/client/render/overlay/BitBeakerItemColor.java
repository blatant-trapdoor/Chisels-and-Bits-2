package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.registry.BagBeakerColours;

public class BitBeakerItemColor implements IItemColor {
    @Override
    public int getColor(ItemStack stack, int tint) {
        if (tint != 1) return -1;
        BagBeakerColours colour = ChiselsAndBits2.getInstance().getItems().getBagBeakerColour(stack);
        return colour != null ? colour.getColour() : -1;
    }
}
