package nl.dgoossens.chiselsandbits2.api.item.property;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.item.DyedItemColour;

public class ColourProperty implements IItemProperty<DyedItemColour> {
    private final DyedItemColour defaultColour;
    public ColourProperty(final DyedItemColour defaultColour) {
        this.defaultColour = defaultColour;
    }

    @Override
    public DyedItemColour get(ItemStack stack) {
        return defaultColour;
    }

    @Override
    public void set(World world, ItemStack stack, DyedItemColour value) {

    }
}
