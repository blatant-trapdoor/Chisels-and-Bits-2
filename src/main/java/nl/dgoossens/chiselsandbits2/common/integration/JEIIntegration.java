package nl.dgoossens.chiselsandbits2.common.integration;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.registry.ModItems;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(ChiselsAndBits2.MOD_ID, "main");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ModItems m = ChiselsAndBits2.getInstance().getItems();
        addDescriptions(registration, m.CHISEL, m.PATTERN, m.SAW, m.TAPE_MEASURE, m.WRENCH,
                m.BIT_BAG, m.BIT_BEAKER, m.MALLET, m.BLUEPRINT, m.MORPHING_BIT,
                m.OAK_PALETTE, m.BIRCH_PALETTE, m.SPRUCE_PALETTE, m.ACACIA_PALETTE, m.JUNGLE_PALETTE, m.DARK_OAK_PALETTE);
    }

    private void addDescriptions(IRecipeRegistration registration, Item... items) {
        IIngredientType<ItemStack> i = registration.getIngredientManager().getIngredientType(ItemStack.class);
        for (Item it : items) {
            if(it == null) continue;
            registration.addIngredientInfo(new ItemStack(it), i, "jei." + it.getTranslationKey());
        }
    }
}
