package nl.dgoossens.chiselsandbits2.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

public class ItemModels extends ItemModelProvider {
    public ItemModels(DataGenerator dataGenerator, ExistingFileHelper existingFileHandler) {
        super(dataGenerator, ChiselsAndBits2.MOD_ID, existingFileHandler);
    }

    @Override
    protected void registerModels() {
        withExistingParent("tool", "item/handheld")
                .transforms()
                    .transform(ModelBuilder.Perspective.THIRDPERSON_RIGHT)
                        .rotation(0, -90, 55)
                        .translation(0, 3.5f, 0.75f)
                        .scale(0.55f, 0.55f, 0.55f)
                    .end()
                    .transform(ModelBuilder.Perspective.THIRDPERSON_LEFT)
                        .rotation(0, 90, 55)
                        .translation(0, 3.5f, 0.75f)
                        .scale(0.55f, 0.55f, 0.55f)
                    .end().end();
        ResourceLocation tool = new ResourceLocation(ChiselsAndBits2.MOD_ID, "tool");

        withExistingParent("bit_bag", "item/generated")
                .texture("layer0", new ResourceLocation(ChiselsAndBits2.MOD_ID, "item/bit_bag"));

        withExistingParent("tape_measure", "item/generated")
                .texture("layer0", new ResourceLocation(ChiselsAndBits2.MOD_ID, "item/tape_measure"));

        withExistingParent("wrench", tool)
                .texture("layer0", new ResourceLocation(ChiselsAndBits2.MOD_ID, "item/wrench"));

        //Colored Bit Bags
        coloredBitBag("black");
        coloredBitBag("blue");
        coloredBitBag("brown");
        coloredBitBag("cyan");
        coloredBitBag("gray");
        coloredBitBag("green");
        coloredBitBag("light_blue");
        coloredBitBag("light_gray");
        coloredBitBag("lime");
        coloredBitBag("magenta");
        coloredBitBag("orange");
        coloredBitBag("pink");
        coloredBitBag("purple");
        coloredBitBag("red");
        coloredBitBag("white");
        coloredBitBag("yellow");
    }

    //Utility method to generate colored bit bag files easily
    private void coloredBitBag(String id) {
        withExistingParent(id+"_bit_bag", "item/generated")
                .texture("layer0", new ResourceLocation(ChiselsAndBits2.MOD_ID, "item/bit_bag_string"))
                .texture("layer1", new ResourceLocation(ChiselsAndBits2.MOD_ID, "item/bit_bag_dyeable"));
    }

    @Override
    public String getName() {
        return "ItemModels";
    }
}
