package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.IItemScrollWheel;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.utils.ItemModeUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.List;

public class ChiseledBlockItem extends BlockItem implements IItemScrollWheel, IItemMenu {
    public ChiseledBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "chiseled_block.help"
        );
    }

    @Override
    public IItemModeType getAssociatedType() {
        return ItemModeType.CHISELED_BLOCK;
    }

    @Override
    public boolean showIconInHotbar() {
        return false;
    }

    //--- COPIED FROM TypedItem.class ---
    /**
     * Display the mode in the highlight tip. (and color for tape measure)
     */
    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        IItemMode im = ItemModeUtil.getChiseledBlockMode(Minecraft.getInstance().player);
        return displayName + " - " + im.getLocalizedName();
    }

    /**
     * Scrolling on the chisel scrolls through the possible modes, alternative to the menu.
     */
    @Override
    public boolean scroll(final PlayerEntity player, final ItemStack stack, final double dwheel) {
        final IItemMode mode = ItemModeUtil.getHeldItemMode(player);
        ItemModeUtil.scrollOption(player, mode, stack, dwheel);
        return true;
    }
}
