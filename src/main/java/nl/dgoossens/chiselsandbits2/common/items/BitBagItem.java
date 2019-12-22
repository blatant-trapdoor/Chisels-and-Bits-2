package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.interfaces.IColourable;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.List;

public class BitBagItem extends StorageItem implements IColourable {
    @Override
    public IItemModeType getAssociatedType() {
        return ItemModeType.SELECTED;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "bit_bag.help",
                ChiselsAndBits2.getInstance().getKeybindings().modeMenu);
    }
}
