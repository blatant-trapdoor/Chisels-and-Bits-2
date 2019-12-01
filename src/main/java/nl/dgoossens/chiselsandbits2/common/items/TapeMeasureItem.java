package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;
import nl.dgoossens.chiselsandbits2.client.gui.RadialMenu;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.MenuAction;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TapeMeasureItem extends TypedItem {
    public TapeMeasureItem(Properties builder) {
        super(builder);
    }

    @Override
    public boolean showIconInHotbar() {
        return false;
    }

    @Override
    public IItemModeType getAssociatedType() {
        return ItemModeType.TAPEMEASURE;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "tape_measure.help",
                Minecraft.getInstance().gameSettings.keyBindUseItem,
                ChiselsAndBits2.getInstance().getKeybindings().clearTapeMeasure,
                ChiselsAndBits2.getInstance().getKeybindings().modeMenu
        );
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (context.getWorld().isRemote) {
            ChiselsAndBits2.getInstance().getClient().useTapeMeasure(new BlockRayTraceResult(context.getHitVec(), context.getFace(), context.getPos(), false));
            return ActionResultType.SUCCESS;
        }
        return super.onItemUse(context);
    }

    @Override
    public Set<RadialMenu.MenuButton> getMenuButtons(final ItemStack item) {
        Set<RadialMenu.MenuButton> ret = new HashSet<>();
        final int colorSize = DyeColor.values().length / 4 * 24 - 4;
        double underring = -RadialMenu.RING_OUTER_EDGE - 34;
        double bntPos = -colorSize;
        final int bntSize = 24;
        Direction textSide = Direction.UP;
        for (final DyeColor color : DyeColor.values()) {
            final MenuAction action = MenuAction.valueOf(color.name());
            if (bntPos > colorSize) {
                underring = RadialMenu.RING_OUTER_EDGE;
                bntPos = -colorSize;
                textSide = Direction.DOWN;
            }
            ret.add(new RadialMenu.MenuButton(action, bntPos, underring, action.getColour(), textSide));
            bntPos += bntSize;
        }
        return ret;
    }
}
