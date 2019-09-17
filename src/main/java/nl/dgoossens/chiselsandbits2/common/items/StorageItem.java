package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.registry.ModItemGroups;

public abstract class StorageItem extends TypedItem {
    public StorageItem() {
        super(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, final CompoundNBT nbt) {
        return new StorageCapabilityProvider();
    }
}
