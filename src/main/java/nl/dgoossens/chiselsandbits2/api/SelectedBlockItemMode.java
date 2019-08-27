package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

public class SelectedBlockItemMode implements IItemMode {
    public static final SelectedBlockItemMode NONE = new SelectedBlockItemMode(null);

    private ResourceLocation key;
    private SelectedBlockItemMode(final ResourceLocation key) {
        this.key = key;
    }

    public static SelectedBlockItemMode fromName(final String key) {
        if(key.equalsIgnoreCase("null")) return NONE;
        return new SelectedBlockItemMode(ResourceLocation.create(key, ':'));
    }
    public static SelectedBlockItemMode fromBlock(final Block blk) {
        return new SelectedBlockItemMode(blk.getRegistryName());
    }
    public ItemStack getStack() { return new ItemStack(ForgeRegistries.BLOCKS.getValue(key)); }

    public String getLocalizedName() {
        return key==null ? I18n.format("general."+ ChiselsAndBits2.MOD_ID +".empty_slot") : I18n.format("block."+key.getNamespace()+"."+key.getPath());
    }

    public String getTypelessName() { return getName(); }

    public String getName() {
        return key==null ? "null" : key.toString();
    }

    public ItemModeType getType() {
        return ItemModeType.SELECTED_BLOCK;
    }
}
