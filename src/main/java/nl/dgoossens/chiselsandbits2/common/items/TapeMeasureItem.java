package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.List;

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
}
