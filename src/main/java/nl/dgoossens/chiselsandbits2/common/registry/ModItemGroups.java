package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

public class ModItemGroups {
    /**
     * The base item group all chisels & bits items are in.
     */
    public static final ItemGroup CHISELS_AND_BITS2 = new ItemGroup("chiselsandbits2") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ChiselsAndBits2.getInstance().getRegister().CHISEL.get());
        }

        @Override
        public boolean hasSearchBar() {
            return true;
        }

        @Override
        public int getSearchbarWidth() {
            return 74; //15 less than normal because our tab name is slightly longer
        }

        @Override
        public ResourceLocation getBackgroundImage() {
            return new ResourceLocation(ChiselsAndBits2.MOD_ID + ":textures/gui/tab_creative.png");
        }
    };
}
