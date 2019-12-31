package nl.dgoossens.chiselsandbits2.api.item.property;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.api.item.IItemModeType;

public class ItemModeProperty implements IItemProperty<IItemMode> {
    private  final IItemModeType type;
    public ItemModeProperty(final IItemModeType type) {
        this.type = type;
    }

    @Override
    public IItemMode get(final ItemStack stack) {
        return type.getDefault();
    }

    @Override
    public void set(World world, ItemStack stack, IItemMode value) {

    }
}
