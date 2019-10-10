package nl.dgoossens.chiselsandbits2.datagen;

import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapelessRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.common.registry.BagBeakerColours;
import nl.dgoossens.chiselsandbits2.common.registry.ModItems;

import java.util.Arrays;
import java.util.function.Consumer;

public class Recipes extends RecipeProvider {
    public Recipes(DataGenerator gen) { super(gen); }

    /*
            General Note on how C&B2 uses the recipe unlocks/advancements:

                All recipes except for the chisel depend on you having a chisel in your inventory.
                The chisel unlocks when you obtain a gold ingot, this allows people unfamiliar with the
                mod to find the chisel item and see all possible other recipes after crafting the chisel.
                The coloured bit bags/tinted bit beakers unlock after you unlock the bit bag/bit beaker so you
                make those first and then discover you can dye them. (they are also grouped to not spam the menu)
                The palettes unlock when you have the slab involved and the chisel.
     */

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        //Palette Recipes
        for(Item slab : Arrays.asList(Items.OAK_SLAB, Items.BIRCH_SLAB, Items.SPRUCE_SLAB, Items.JUNGLE_SLAB, Items.ACACIA_SLAB, Items.DARK_OAK_SLAB)) {
            String s = slab.getRegistryName().getPath().toUpperCase();
            s = s.substring(0, s.length() - "_SLAB".length());
            //S is now OAK, BIRCH, SPRUCE, JUNGLE, etc.
            ModItems i = ChiselsAndBits2.getInstance().getItems();
            ShapelessRecipeBuilder.shapelessRecipe(i.getPalette(s+"_PALETTE"))
                    .addIngredient(slab)
                    .addIngredient(Tags.Items.DYES)
                    .addIngredient(Tags.Items.DYES)
                    .addIngredient(Tags.Items.DYES)
                    .setGroup("palettes")
                    .addCriterion("has_a_chisel", InventoryChangeTrigger.Instance.forItems(i.CHISEL))
                    .addCriterion("slab", InventoryChangeTrigger.Instance.forItems(slab))
                    .build(consumer);
        }

        //Bit Bag & Beaker Recipes
        for(BagBeakerColours colour : BagBeakerColours.values()) {
            //Get the minecraft dye item corresponding to the colour
            Item dye = ForgeRegistries.ITEMS.getValue(new ResourceLocation("minecraft", colour.name().toLowerCase()+"_dye"));
            ModItems i = ChiselsAndBits2.getInstance().getItems();

            //Bit Bag Recipe
            ShapelessRecipeBuilder.shapelessRecipe(i.getBitBag(colour))
                    .addIngredient(i.BIT_BAG)
                    .addIngredient(dye)
                    .setGroup("dyed_bit_bags")
                    .addCriterion("bit_bag", InventoryChangeTrigger.Instance.forItems(i.BIT_BAG))
                    .build(consumer);

            //Bit Beaker Recipe
            ShapelessRecipeBuilder.shapelessRecipe(i.getBitBeaker(colour))
                    .addIngredient(i.BIT_BEAKER)
                    .addIngredient(dye)
                    .setGroup("tinted_bit_beakers")
                    .addCriterion("bit_beaker", InventoryChangeTrigger.Instance.forItems(i.BIT_BEAKER))
                    .build(consumer);
        }
    }
}
