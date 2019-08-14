package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.IItemScrollWheel;
import nl.dgoossens.chiselsandbits2.api.modes.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.api.modes.ItemMode;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;
import org.apache.commons.lang3.text.WordUtils;

import javax.annotation.Nullable;
import java.util.List;

public class PatternItem extends Item implements IItemScrollWheel, IItemMenu {
    public PatternItem(Properties builder) { super(builder); }

    @Override
    public ItemMode.Type getAssociatedType() {
        return ItemMode.Type.PATTERN;
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "pattern.help",
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                ChiselsAndBits2.getKeybindings().copyPattern,
                ChiselsAndBits2.getKeybindings().modeMenu
        );
    }

    /**
     * Display the pattern mode in the highlight tip.
     */
    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        return displayName + " - " + ItemMode.getMode(item).getLocalizedName();
    }

    /**
     * Scrolling on the chisel scrolls through the possible pattern modes, alternative to the menu.
     */
    @Override
    public boolean scroll(final PlayerEntity player, final ItemStack stack, final double dwheel) {
        final ItemMode mode = ChiselModeManager.getMode(player);
        ChiselModeManager.scrollOption(ItemMode.Type.PATTERN, mode, dwheel);
        return true;
    }
}
