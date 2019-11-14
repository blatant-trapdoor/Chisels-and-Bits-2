package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitStorage;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.ItemMode;
import nl.dgoossens.chiselsandbits2.api.VoxelWrapper;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.impl.ChiselModeManager;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.items.StorageItem;

import java.util.*;

/**
 * Utilities for managing durability usage and bag contents.
 */
public class InventoryUtils {
    /**
     * Builds a calculated inventory which can be queried for information. The calculated inventory should be seen as a snapshot
     * taken at a time. It ignores any changes to the inventory unrelated to this calculation.
     *
     * This method should be called on the server otherwise changes won't synchronize.
     */
    public static CalculatedInventory buildInventory(PlayerEntity player) {
        return new CalculatedInventory(player);
    }

    /**
     * A calculated inventory object which tracks the current storage for bits and available durability.
     * Changes made to the CalculatedInventory happen to the real inventory (when apply() is called), changes to the real inventory do not change the calculated inventory!
     * aka this is a kind of cache to avoid having to redetermine the available durability for each individual bit modified
     */
    public static class CalculatedInventory {
        private PlayerEntity player;
        private long durability, usedDurability;
        private long bitsAvailable, bitsUsed;
        private VoxelWrapper bitPlaced;
        private Map<Integer, BitStorage> bitStorages = new HashMap<>();
        private Set<Integer> modifiedBitStorages = new HashSet<>();

        public CalculatedInventory(PlayerEntity player) {
            this.player = player;

            //Calculate durability
            for (ItemStack item : player.inventory.mainInventory) {
                if (item.getItem() instanceof ChiselItem)
                    durability += (item.getMaxDamage() - item.getDamage());
            }
            for (ItemStack item : player.inventory.offHandInventory) {
                if (item.getItem() instanceof ChiselItem)
                    durability += (item.getMaxDamage() - item.getDamage());
            }

            //Scan for storages
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                ItemStack item = player.inventory.mainInventory.get(i);
                if(item.getItem() instanceof StorageItem)
                    bitStorages.put(i, item.getCapability(StorageCapabilityProvider.STORAGE).orElse(null));
            }
            for (int i = 0; i < player.inventory.offHandInventory.size(); i++) {
                ItemStack item = player.inventory.offHandInventory.get(i);
                //Negative slot numbers for offhand item(s)
                if(item.getItem() instanceof StorageItem)
                    bitStorages.put(-i, item.getCapability(StorageCapabilityProvider.STORAGE).orElse(null));
            }

            bitStorages.entrySet().removeIf(e -> e.getValue() == null);
        }

        /**
         * Starts tracking how much of the bit type to place is stored, these will be used
         * when placement happens.
         */
        public void trackMaterialUsage(VoxelWrapper wrapper) {
            bitPlaced = wrapper;

            //Calculate available bits
            for(BitStorage store : bitStorages.values())
                bitsAvailable += store.get(wrapper);
        }

        /**
         * Returns whether there is still at least one bit worth of material to use.
         */
        public boolean hasMaterial() {
            if(player.isCreative()) return true;
            return bitsUsed < bitsAvailable;
        }

        /**
         * Extracts and replaces the bit in the voxelblob at the specified coordinates.
         */
        public void extractBit(VoxelBlob voxelBlob, int x, int y, int z) {
            if(!player.isCreative()) {
                if(bitsUsed >= bitsAvailable) return;
                bitsUsed++;
            }
            voxelBlob.set(x, y, z, bitPlaced.getId());
        }

        /**
         * Gets the total available materials.
         */
        public long getAvailableMaterial() {
            return bitsAvailable;
        }


        /**
         * Gets the total available durability.
         */
        public long getAvailableDurability() {
            return durability;
        }

        /**
         * Removes 1 point worth of durability from the chisels in the inventory. Chisel in selected slot is preferred.
         * Items are moved to keep a chisel in the selected slot if there is already a chisel in the selected slot.
         * This method also adds one use to the chisel item use statistic.
         * @return False if the inventory didn't have any chisels available.
         */
        public boolean damageChisel() {
            //We ignore chisel durability completely in creative.
            if(player.isCreative()) {
                usedDurability++; //for the statistics
                return true;
            }

            if(usedDurability >= durability) return false;
            usedDurability++;
            return true;
        }

        /**
         * Applies all changes to the real player's inventory.
         * Desyncs are ignored in the player's favor. (e.g. if the total durability decreased since the calculated inventory was made we don't mind the difference and take as much as available)
         */
        public void apply() {
            applyDurabilityChanges();
            applyMaterialUsage();
            applyMaterialGain();

            //Send updates for all modified bit bags
            for(int i : modifiedBitStorages) {
                if(i < 0) {
                    ChiselModeManager.updateStackCapability(player.inventory.offHandInventory.get(-i), bitStorages.get(i), player);
                    continue;
                }
                ChiselModeManager.updateStackCapability(player.inventory.mainInventory.get(i), bitStorages.get(i), player);
            }
            modifiedBitStorages.clear();
        }

        /**
         * Removes materials from all bit storages in the inventory.
         */
        private void applyMaterialUsage() {
            if(player.isCreative()) return; //We don't need to take materials for creative players.
            if(bitsUsed > 0) {
                for(int slot : bitStorages.keySet()) {
                    if(bitsUsed <= 0) break;

                    BitStorage bs = bitStorages.get(slot);
                    if(!bs.has(bitPlaced)) continue;
                    long capacity = Math.min(bitsUsed, bs.get(bitPlaced));
                    bs.add(bitPlaced, -capacity);
                    modifiedBitStorages.add(slot);
                    bitsUsed -= capacity;

                    //TODO if this causes the bag to no longer have this type of bit, select the same bit type on another bag containing this type to avoid random switching
                }
            }
        }

        /**
         * Adds extracted materials to available bit storages.
         */
        private void applyMaterialGain() {

        }

        /**
         * Applies durability damage to the chisels in the inventory of the player.
         */
        private void applyDurabilityChanges() {
            ItemStack selectedItem = player.getHeldItemMainhand();
            if (usedDurability > 0) {
                //Update the bits chiseled statistic
                player.addStat(ChiselsAndBits2.getInstance().getStatistics().BITS_CHISELED, (int) usedDurability);

                if (player.isCreative()) return; //We don't need to do the rest for creative players.

                boolean holdingChisel = selectedItem.getItem() instanceof ChiselItem;
                int targetSlot = player.inventory.currentItem;
                if (!holdingChisel) targetSlot = findNextChisel(player.inventory, -1);
                if (targetSlot == -1)
                    return; //This shouldn't happen, but it might, anyways we don't care for a little bit of durability theft so we don't need to force take durability.

                //We store the previous chisel's mode here to keep the mode through chisel changes. (just a lil quality 'o life)
                IItemMode oldMode = null;
                //Now we start taking durability from the chisel in our targetSlot and otherwise scan for other chisels to move to our targetSlot.
                while (usedDurability > 0) {
                    ItemStack target = player.inventory.getStackInSlot(targetSlot);

                    //Check if the item is valid, otherwise move a chisel in from elsewhere.
                    if (target.isEmpty() || !(target.getItem() instanceof ChiselItem)) {
                        //Find a new chisel to move to the targetSlot
                        int foundChisel = findNextChisel(player.inventory, targetSlot);
                        if (foundChisel == -1)
                            return; //If we can't find a chisel we ignore the remaining durability debt.

                        ItemStack newChisel = player.inventory.getStackInSlot(foundChisel);
                        if (oldMode != null)
                            ChiselModeManager.setMode(player, newChisel, oldMode);
                        player.inventory.removeStackFromSlot(foundChisel);
                        player.inventory.setInventorySlotContents(targetSlot, newChisel);
                        if (!target.isEmpty())
                            //If the target is somehow a valid item, try to get it back into the inventory, we don't care if this fails. Target should never not be empty.
                            player.inventory.addItemStackToInventory(target);

                        target = player.inventory.getStackInSlot(targetSlot);
                    }

                    //Prevent unnecessary mode fetching.
                    oldMode = !(target.getItem() instanceof IItemMenu) ? oldMode : ChiselModeManager.getMode(target);
                    //Get the maximum we can take from this chisel.
                    long capacity = Math.min(usedDurability, target.getMaxDamage() - target.getDamage());
                    //Take durability from the current item.
                    target.damageItem((int) capacity, player, (p) -> p.sendBreakAnimation(Hand.MAIN_HAND));
                    usedDurability -= capacity;
                }
            }
        }

        /**
         * Finds the first slot a chisel is in.
         * @param exclude A specific slot to be excluded from the search.
         * @return -1 if no chisel can be found.
         */
        private int findNextChisel(PlayerInventory inventory, int exclude) {
            for(int i = 0; i < inventory.mainInventory.size(); i++) {
                if(i == exclude) continue;
                if(player.inventory.mainInventory.get(i).getItem() instanceof ChiselItem)
                    return i;
            }
            return -1;
        }
    }
}
