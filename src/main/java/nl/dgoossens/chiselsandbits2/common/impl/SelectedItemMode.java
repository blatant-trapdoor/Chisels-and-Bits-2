package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.IItemModeType;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.api.VoxelWrapper;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.utils.BitUtil;

import java.awt.*;

public class SelectedItemMode implements IItemMode {
    /**
     * The default none item mode.
     */
    public static final SelectedItemMode NONE = new SelectedItemMode(VoxelBlob.AIR_BIT) {
        @Override
        public String getLocalizedName() {
            return I18n.format("general." + ChiselsAndBits2.MOD_ID + ".empty_slot");
        }

        @Override
        public String getName() {
            return "none";
        }

        @Override
        public boolean isNone() {
            return true; //Overwriting is actually the fastest way to do this.
        }
    };
    private int value;
    private VoxelType type;

    private SelectedItemMode(final int value) {
        this.value = value;
        type = VoxelType.getType(value);
    }

    /**
     * Get the voxel type of this item mode.
     */
    public VoxelType getVoxelType() {
        return type;
    }

    /**
     * Gets this selected item mode's voxel wrapper.
     */
    public VoxelWrapper getVoxelWrapper() {
        return VoxelWrapper.forAbstract(value);
    }

    /**
     * Get the item mode type.
     */
    public IItemModeType getType() {
        return ItemModeType.SELECTED;
    }

    /**
     * Get the item stack to be shown when this mode is selected.
     */
    public ItemStack getStack() {
        return type==VoxelType.BLOCKSTATE ? new ItemStack(getBlock()) : new ItemStack(getFluid().getFilledBucket());
    }

    /**
     * Get the block associated to this item mode if this is a selected block.
     */
    public Block getBlock() {
        if(type != VoxelType.BLOCKSTATE) return Blocks.AIR;
        return BitUtil.getBlockState(value).getBlock();
    }

    /**
     * Get the fluid associated to this item mode if this is a selected fluid.
     */
    public Fluid getFluid() {
        if(type != VoxelType.FLUIDSTATE) return Fluids.EMPTY;
        return BitUtil.getFluidState(value).getFluid();
    }

    /**
     * Get the colour associated to this item mode if this is a selected colour.
     */
    public Color getColour() {
        if(type != VoxelType.COLOURED) return Color.BLACK;
        return BitUtil.getColourState(value);
    }

    /**
     * Gets the name of this selected item mode.
     */
    public String getLocalizedName() {
        switch(type) {
            case COLOURED:
                Color c = getColour();
                return "("+c.getRed()+","+c.getGreen()+","+c.getBlue()+")";
            case BLOCKSTATE:
                Block b = getBlock();
                return I18n.format("block" + "." + b.getRegistryName().getNamespace() + "." + b.getRegistryName().getPath());
            case FLUIDSTATE:
                Fluid f = getFluid();
                return I18n.format("block" + "." + f.getRegistryName().getNamespace() + "." + f.getRegistryName().getPath());
        }
        return "";
    }

    /**
     * Gets the name of this selected item mode.
     */
    public String getTypelessName() {
        return getLocalizedName();
    }

    /**
     * Returns an empty string as the name isn't used for selected item modes.
     */
    public String getName() {
        return "";
    }

    /**
     * Get the bit id of this mode.
     */
    public int getBitId() {
        return value;
    }

    /**
     * Get the bit id that should be placed.
     * This factors in blockstate properties, like leaves will always place as 0 distance from log
     * or how logs will place rotated.
     */
    public int getPlacementBitId(BlockItemUseContext context) {
        switch(type) {
            case BLOCKSTATE:
                return BitUtil.getBlockId(getBlock().getStateForPlacement(context));
            default:
                return value;
        }
    }

    /**
     * Returns whether this is the none mode.
     */
    public boolean isNone() {
        return false;
    }

    @Override
    public String toString() {
        return getLocalizedName();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SelectedItemMode) {
            SelectedItemMode s = (SelectedItemMode) obj;
            return s.value == value;
        }
        return super.equals(obj);
    }

    @Override
    public int getDynamicId() {
        return value;
    }

    /**
     * Check if a item mode is the none selected item mode.
     * Also works if this ItemMode is of the ItemMode enum.
     */
    public static boolean isNone(IItemMode mode) {
        if(mode instanceof SelectedItemMode) return ((SelectedItemMode) mode).isNone();
        return false;
    }

    /**
     * Builds a selected item mode from a block/fluid resource name.
     */
    public static SelectedItemMode fromName(final String key, final boolean fluid) {
        if (key.equalsIgnoreCase("null")) return NONE;
        return new SelectedItemMode(fluid ? BitUtil.getFluidId(ForgeRegistries.FLUIDS.getValue(ResourceLocation.create(key, ':')).getDefaultState()) : BitUtil.getBlockId(ForgeRegistries.BLOCKS.getValue(ResourceLocation.create(key, ':')).getDefaultState()));
    }

    /**
     * Builds a selected item mode from a given bit type.
     */
    public static SelectedItemMode fromVoxelType(final int voxel) {
        return new SelectedItemMode(voxel);
    }

    /**
     * Builds a selected item mode from a given voxel wrapper.
     */
    public static SelectedItemMode fromVoxelWrapper(final VoxelWrapper wrapper) {
        return new SelectedItemMode(wrapper.getId());
    }
}
