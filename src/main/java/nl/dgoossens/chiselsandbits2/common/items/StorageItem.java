package nl.dgoossens.chiselsandbits2.common.items;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitStorage;
import nl.dgoossens.chiselsandbits2.api.ItemModeType;
import nl.dgoossens.chiselsandbits2.api.SelectedItemMode;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.registry.ModItemGroups;

public abstract class StorageItem extends TypedItem {
    public StorageItem() {
        super(new Item.Properties().maxStackSize(1).group(ModItemGroups.CHISELS_AND_BITS2));
    }

    @Override
    public ICapabilityProvider initCapabilities(final ItemStack stack, final CompoundNBT nbt) {
        return new StorageCapabilityProvider();
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return !SelectedItemMode.isNone(ChiselModeManager.getSelectedItem(stack)) && ChiselsAndBits2.getInstance().getConfig().showBitsAvailableAsDurability.get();
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        SelectedItemMode s = ChiselModeManager.getSelectedItem(stack);
        BitStorage store = stack.getCapability(StorageCapabilityProvider.STORAGE).orElse(null);
        if(store == null) return 0;
        return (double) (ChiselsAndBits2.getInstance().getConfig().bitsPerTypeSlot.get() - (s.getType() == ItemModeType.SELECTED_BLOCK ? store.getAmount(s.getBlock()) : store.getAmount(s.getFluid()))) / (double) ChiselsAndBits2.getInstance().getConfig().bitsPerTypeSlot.get();
    }
}
