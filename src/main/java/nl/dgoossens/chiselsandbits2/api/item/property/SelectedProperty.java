package nl.dgoossens.chiselsandbits2.api.item.property;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.bit.VoxelWrapper;

import java.util.function.Supplier;

public class SelectedProperty implements IItemProperty<VoxelWrapper> {
    private final Supplier<VoxelWrapper> defaultValue;
    public SelectedProperty(final Supplier<VoxelWrapper> defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public VoxelWrapper get(ItemStack stack) {

        return defaultValue.get();
    }

    @Override
    public void set(World world, ItemStack stack, VoxelWrapper value) {

    }
}
