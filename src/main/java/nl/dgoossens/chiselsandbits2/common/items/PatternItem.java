package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IRotatableItem;
import nl.dgoossens.chiselsandbits2.client.gui.ItemModeMenu;
import nl.dgoossens.chiselsandbits2.common.impl.item.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.item.MenuAction;
import nl.dgoossens.chiselsandbits2.common.util.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatternItem extends TypedItem {
    public PatternItem(Properties builder) {
        super(builder);
    }

    @Override
    public boolean showIconInHotbar() {
        return true;
    }

    @Override
    public IItemModeType getAssociatedType() {
        return ItemModeType.PATTERN;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "pattern.help",
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                ChiselsAndBits2.getInstance().getKeybindings().modeMenu
        );
    }
}
