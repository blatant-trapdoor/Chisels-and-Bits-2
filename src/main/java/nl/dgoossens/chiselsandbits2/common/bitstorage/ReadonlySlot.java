package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class ReadonlySlot extends Slot {
    public ReadonlySlot(final IInventory inv, final int index, final int xPos, final int yPos) {
        super(inv, index, xPos, yPos);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeStack(PlayerEntity playerIn) {
        return false;
    }
}
