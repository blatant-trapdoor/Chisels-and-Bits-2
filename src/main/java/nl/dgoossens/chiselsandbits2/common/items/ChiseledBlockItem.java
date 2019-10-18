package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.List;

public class ChiseledBlockItem extends BlockItem {
    public ChiseledBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "chiseled_block.help"
        );
    }
}
