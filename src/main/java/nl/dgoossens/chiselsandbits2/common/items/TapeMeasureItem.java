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

public class TapeMeasureItem extends Item implements IItemScrollWheel, IItemMenu {
    public TapeMeasureItem(Properties builder) { super(builder); }

    @Override
    public ItemMode.Type getAssociatedType() {
        return ItemMode.Type.TAPEMEASURE;
    }
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "tape_measure.help",
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                ChiselsAndBits2.getKeybindings().clearTapeMeasure,
                ChiselsAndBits2.getKeybindings().modeMenu
        );
    }

    /**
     * Display the tape measure mode in the highlight tip.
     */
    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        return displayName + " - " + WordUtils.capitalizeFully(ItemMode.getMode(item).name());
    }

    /**
     * Scrolling on the chisel scrolls through the possible tape measure modes, alternative to the menu.
     */
    @Override
    public boolean scroll(final PlayerEntity player, final ItemStack stack, final double dwheel) {
        final ItemMode mode = ChiselModeManager.getMode(player);
        ChiselModeManager.scrollOption(ItemMode.Type.TAPEMEASURE, mode, dwheel);
        return true;
    }
}
