package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.*;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IBitModifyItem;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IItemScrollWheel;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IRotatableItem;
import nl.dgoossens.chiselsandbits2.api.item.attributes.IVoxelStorer;
import nl.dgoossens.chiselsandbits2.client.gui.ItemModeMenu;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.NBTBlobConverter;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelVersions;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;
import nl.dgoossens.chiselsandbits2.common.impl.MenuAction;
import nl.dgoossens.chiselsandbits2.common.utils.ChiselUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemPropertyUtil;
import nl.dgoossens.chiselsandbits2.common.utils.ItemTooltipWriter;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChiseledBlockItem extends BlockItem implements IItemScrollWheel, IItemMenu, IRotatableItem, IBitModifyItem, IVoxelStorer {
    public ChiseledBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ItemTooltipWriter.addItemInformation(tooltip, "chiseled_block.help",
                ChiselsAndBits2.getInstance().getKeybindings().modeMenu
        );
    }

    @Override
    public boolean canPerformModification(ModificationType type) {
        return type == ModificationType.PLACE;
    }

    @Override
    public VoxelBlob getVoxelBlob(ItemStack stack) {
        final NBTBlobConverter c = new NBTBlobConverter();
        c.readChiselData(stack.getOrCreateChildTag(ChiselUtil.NBT_BLOCKENTITYTAG), VoxelVersions.getDefault());
        return c.getVoxelBlob();
    }

    @Override
    public void rotate(ItemStack stack, Direction.Axis axis) {
        final NBTBlobConverter c = new NBTBlobConverter();
        c.readChiselData(stack.getOrCreateChildTag(ChiselUtil.NBT_BLOCKENTITYTAG), VoxelVersions.getDefault());

        final VoxelBlob vb = c.getVoxelBlob();
        c.setBlob(vb.spin(axis));

        final CompoundNBT nbt = new CompoundNBT();
        c.writeChiselData(nbt);

        stack.setTagInfo(ChiselUtil.NBT_BLOCKENTITYTAG, nbt);
    }

    @Override
    public IItemModeType getAssociatedType() {
        return ItemModeType.CHISELED_BLOCK;
    }

    @Override
    public boolean showIconInHotbar() {
        return false;
    }

    @Override
    public Set<ItemModeMenu.MenuButton> getMenuButtons(final ItemStack item) {
        Set<ItemModeMenu.MenuButton> ret = new HashSet<>();
        ret.add(new ItemModeMenu.MenuButton(MenuAction.ROLL_X, -ItemModeMenu.TEXT_DISTANCE - 18, -20, Direction.WEST));
        ret.add(new ItemModeMenu.MenuButton(MenuAction.ROLL_Z, -ItemModeMenu.TEXT_DISTANCE - 18, 4, Direction.WEST));
        return ret;
    }

    //--- COPIED FROM TypedItem.class ---
    /**
     * Display the mode in the highlight tip. (and color for tape measure)
     */
    @Override
    public String getHighlightTip(ItemStack item, String displayName) {
        IItemMode im = ItemPropertyUtil.getChiseledBlockMode();
        return displayName + " - " + im.getLocalizedName();
    }

    /**
     * Scrolling on the chisel scrolls through the possible modes, alternative to the menu.
     */
    @Override
    public boolean scroll(final PlayerEntity player, final ItemStack stack, final double dwheel) {
        return TypedItem.scroll(player, stack, dwheel, ItemPropertyUtil.getChiseledBlockMode(), getAssociatedType());
    }
}
