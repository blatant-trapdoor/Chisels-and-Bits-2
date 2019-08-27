package nl.dgoossens.chiselsandbits2.client.render.overlay;

import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.MenuAction;

public class ItemColorBitBag implements IItemColor {
    @Override
    public int getColor(ItemStack stack, int tint) {
        if(tint!=1) return -1;
        MenuAction colour = ChiselsAndBits2.getItems().getBitBagColour(stack);
        return colour!=null ? colour.getColour() : -1;
    }
}
