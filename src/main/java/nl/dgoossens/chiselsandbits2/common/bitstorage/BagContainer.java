package nl.dgoossens.chiselsandbits2.common.bitstorage;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

public class BagContainer extends Container {
    public static final int MAX_SLOTS = 12; //The maximum amount of slots a bag could ever have.
    private Inventory bagInventory;

    public BagContainer()

    public BagContainer(int id, PlayerInventory playerInventory) {
        super(ChiselsAndBits2.getInstance().getContainers().BIT_BAG, id);
        bagInventory = new Inventory(MAX_SLOTS);
        bagInventory.openInventory(playerInventory.player);
        int numRows = (MAX_SLOTS / 9 + 1);
        int i = (numRows - 4) * 18;

        for(int j = 0; j < numRows; ++j) {
            for(int k = 0; k < ((MAX_SLOTS - j * 9) >= 9 ? 9 : MAX_SLOTS % 9); ++k) {
                this.addSlot(new Slot(bagInventory, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        for(int l = 0; l < 3; ++l) {
            for(int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < this.bagInventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemstack1, this.bagInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, this.bagInventory.getSizeInventory(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        bagInventory.closeInventory(playerIn);
    }
}
