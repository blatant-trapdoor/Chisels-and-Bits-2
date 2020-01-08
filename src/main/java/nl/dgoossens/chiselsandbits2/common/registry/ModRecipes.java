package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.items.recipes.RecoloringRecipe;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRecipes {
    public final SpecialRecipeSerializer<RecoloringRecipe> CRAFTING_SPECIAL_RECOLOR = buildRecipeSerializer("crafting_special_recolor", new SpecialRecipeSerializer<>(RecoloringRecipe::new));

    private static <S extends IRecipeSerializer<T>, T extends IRecipe<?>> S buildRecipeSerializer(String key, S recipeSerializer) {
        return (S) recipeSerializer.setRegistryName(new ResourceLocation(ChiselsAndBits2.MOD_ID, key));
    }

    @SubscribeEvent
    public static void onRecipeRegister(final RegistryEvent.Register<IRecipeSerializer<?>> e) {
        final ModRecipes c = ChiselsAndBits2.getInstance().getRecipes();
        e.getRegistry().register(c.CRAFTING_SPECIAL_RECOLOR);
    }
}
