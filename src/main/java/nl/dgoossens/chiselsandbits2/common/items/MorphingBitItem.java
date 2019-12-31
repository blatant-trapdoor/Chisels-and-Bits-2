package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.bit.VoxelWrapper;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IBitModifyItem;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.item.property.SelectedProperty;
import nl.dgoossens.chiselsandbits2.api.item.property.StateProperty;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.utils.ItemPropertyUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.List;

public class MorphingBitItem extends ChiselMimicItem {
    protected int PROPERTY_LOCKED;
    protected int PROPERTY_SELECTED;

    public MorphingBitItem(Properties builder) {
        super(builder);

        PROPERTY_LOCKED = addProperty(new StateProperty(false));
        PROPERTY_SELECTED = addProperty(new SelectedProperty(ItemPropertyUtil::getGlobalSelectedVoxelWrapper));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "morphing_bit.help" + (isLocked(stack) ? ".locked" : ""),
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                Minecraft.getInstance().gameSettings.keyBindAttack
        );
    }

    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        VoxelWrapper s = getSelected(item);
        if(s.isEmpty()) return super.getHighlightTip(item, displayName);
        return super.getHighlightTip(item, displayName) + " - " + s.getDisplayName();
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        if(isLocked(stack)) return new TranslationTextComponent(getTranslationKey() + ".locked");
        return super.getDisplayName(stack);
    }

    public VoxelWrapper getSelected(final ItemStack stack) {
        return getProperty(PROPERTY_SELECTED, VoxelWrapper.class).get(stack);
    }

    public void setSelected(final World world, final ItemStack stack, final VoxelWrapper w) {
        getProperty(PROPERTY_SELECTED, VoxelWrapper.class).set(world, stack, w);
    }

    public boolean isLocked(final ItemStack stack) {
        return getProperty(PROPERTY_LOCKED, Boolean.class).get(stack);
    }
}
