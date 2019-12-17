package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.items.recipes.RecoloringRecipe;

public class ModRecipes {
    public final SpecialRecipeSerializer<RecoloringRecipe> CRAFTING_SPECIAL_RECOLOR = register("crafting_special_recolor", new SpecialRecipeSerializer<>(RecoloringRecipe::new));

    private static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(String key, S recipeSerializer) {
        ForgeRegistries.RECIPE_SERIALIZERS.register(recipeSerializer.setRegistryName(new ResourceLocation(ChiselsAndBits2.MOD_ID, key)));
        return recipeSerializer;
    }
}
