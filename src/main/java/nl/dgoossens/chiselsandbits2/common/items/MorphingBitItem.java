package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.SelectedItemMode;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.List;

public class MorphingBitItem extends TypedItem implements ChiselUtil.BitPlaceItem, ChiselUtil.BitRemoveItem {
    public MorphingBitItem(Properties builder) {
        super(builder);
    }

    @Override
    public boolean showIconInHotbar() {
        return true;
    }

    @Override
    public IItemModeType getAssociatedType() {
        return ItemModeType.CHISEL;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "morphing_bit.help",
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                Minecraft.getInstance().gameSettings.keyBindAttack
        );
    }

    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        SelectedItemMode s = ItemModeUtil.getGlobalSelectedItemMode(Minecraft.getInstance().player);
        if(s.isNone()) return super.getHighlightTip(item, displayName);
        return super.getHighlightTip(item, displayName) + " - " + s.getLocalizedName();
    }
}
