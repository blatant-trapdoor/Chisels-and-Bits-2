package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.List;

public class TapeMeasureItem extends Item {
    public TapeMeasureItem(Properties builder) { super(builder); }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "tape_measure.help",
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                ChiselsAndBits2.getKeybindings().clearTapeMeasure,
                ChiselsAndBits2.getKeybindings().modeMenu
        );
    }
}
