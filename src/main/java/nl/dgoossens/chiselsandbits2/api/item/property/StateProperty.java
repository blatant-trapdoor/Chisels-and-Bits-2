package nl.dgoossens.chiselsandbits2.api.item.property;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class StateProperty implements IItemProperty<Boolean> {
    private final boolean defaultValue;
    public StateProperty(final boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Boolean get(final ItemStack stack) {
        return defaultValue;
    }

    @Override
    public void set(World world, ItemStack stack, Boolean value) {

    }
}
