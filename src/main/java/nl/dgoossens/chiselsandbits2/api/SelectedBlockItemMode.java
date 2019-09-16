package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

public class SelectedBlockItemMode implements IItemMode {
    public static final SelectedBlockItemMode NONE_BAG = new SelectedBlockItemMode(null, false);
    public static final SelectedBlockItemMode NONE_BEAKER = new SelectedBlockItemMode(null, true);

    private ResourceLocation key;
    private boolean fluid;
    private SelectedBlockItemMode(final ResourceLocation key, final boolean fluid) { this.key = key; this.fluid = fluid; }

    public static SelectedBlockItemMode fromName(final String key, final boolean fluid) {
        if(key.equalsIgnoreCase("null")) return fluid ? NONE_BEAKER : NONE_BAG;
        return new SelectedBlockItemMode(ResourceLocation.create(key, ':'), fluid);
    }

    public static SelectedBlockItemMode fromBlock(final Block blk) {
        return new SelectedBlockItemMode(blk.getRegistryName(), false);
    }
    public static SelectedBlockItemMode fromFluid(final Fluid blk) {
        return new SelectedBlockItemMode(blk.getRegistryName(), true);
    }

    public ItemStack getStack() {
        return !fluid ? new ItemStack(ForgeRegistries.BLOCKS.getValue(key)) : new ItemStack(ForgeRegistries.FLUIDS.getValue(key).getFilledBucket());
    }

    public String getLocalizedName() {
        return key==null ? I18n.format("general."+ ChiselsAndBits2.MOD_ID +".empty_slot") : I18n.format((fluid ? "fluid" : "block")+"."+key.getNamespace()+"."+key.getPath());
    }

    public String getTypelessName() { return getName(); }
    public String getName() { return key==null ? "null" : key.toString(); }
    public ItemModeType getType() { return fluid ? ItemModeType.SELECTED_FLUID : ItemModeType.SELECTED_BLOCK; }
}
