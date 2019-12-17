package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.common.items.recipes.RecoloringRecipe;

public class ModRecipes {
    public final SpecialRecipeSerializer<RecoloringRecipe> CRAFTING_SPECIAL_RECOLOR = register(new SpecialRecipeSerializer<>(RecoloringRecipe::new));

    private static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S register(S recipeSerializer) {
        ForgeRegistries.RECIPE_SERIALIZERS.register(recipeSerializer);
        return recipeSerializer;
    }
}
