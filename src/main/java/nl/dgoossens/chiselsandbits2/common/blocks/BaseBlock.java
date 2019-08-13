package nl.dgoossens.chiselsandbits2.common.blocks;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public interface BaseBlock {
    default Item.Properties getItemProperties() { return new Item.Properties(); }
    default BlockItem getBlockItem() { return null; }
}
