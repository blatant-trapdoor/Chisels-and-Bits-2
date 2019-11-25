package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.BitOperation;
import nl.dgoossens.chiselsandbits2.api.BitStorage;
import nl.dgoossens.chiselsandbits2.api.IItemMenu;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.SelectedItemMode;
import nl.dgoossens.chiselsandbits2.api.VoxelType;
import nl.dgoossens.chiselsandbits2.api.VoxelWrapper;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.items.StorageItem;

import java.util.*;
import java.util.function.Function;

import static nl.dgoossens.chiselsandbits2.api.BitOperation.PLACE;
import static nl.dgoossens.chiselsandbits2.api.BitOperation.SWAP;

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
    public static CalculatedInventory buildInventory(ServerPlayerEntity player) {
        return new CalculatedInventory(player);
    }

    /**
     * Builds an inventory snapshot that can be queried for information. Variant of the CalculatedInventory that can only
     * be queried, not modified.
     *
     * @deprecated Draft API: Not fully finished, for now don't try to do these calculations on the client side. Let the server handle it.
     */
    @Deprecated
    public static InventorySnapshot takeSnapshot(PlayerEntity player) {
        return new InventorySnapshot(player);
    }

    /**
     * An inventory snapshot which can only be queried for information. Lite version of the CalculatedInventory.
     */
    public static class InventorySnapshot {
        private PlayerEntity player;
        Map<Integer, BitStorage> bitStorages = new HashMap<>();
        long bitsAvailable, bitsUsed;
        VoxelWrapper bitPlaced;

        public InventorySnapshot(PlayerEntity player) {
            this.player = player;

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
         *
         * Can be set multiple times to switch tracked materials.
         */
        public void trackMaterialUsage(VoxelWrapper wrapper) {
            bitPlaced = wrapper.simplify();
            bitsAvailable = 0;
            bitsUsed = 0;

            //You have infinite coloured bits
            if(VoxelType.isColoured(wrapper.getId())) {
                bitsAvailable = Long.MAX_VALUE;
                return;
            }

            //Calculate available bits
            for(BitStorage store : bitStorages.values())
                bitsAvailable += store.get(wrapper);
        }

        /**
         * Returns whether there is still at least one bit worth of material to use.
         */
        public boolean hasMaterial() {
            if(getPlayer().isCreative()) return true;
            return bitsUsed < bitsAvailable;
        }

        /**
         * Returns whether or not a bit can currently be placed.
         */
        public boolean canPlaceBit() {
            if(!getPlayer().isCreative()) {
                if(bitsUsed >= bitsAvailable) return false;
                bitsUsed++;
            }
            return true;
        }

        /**
         * Gets the total available materials.
         */
        public long getAvailableMaterial() {
            return bitsAvailable;
        }

        /**
         * Get the player involved.
         */
        public PlayerEntity getPlayer() {
           return player;
        }
    }

    /**
     * A calculated inventory object which tracks the current storage for bits and available durability.
     * Changes made to the CalculatedInventory happen to the real inventory (when apply() is called), changes to the real inventory do not change the calculated inventory!
     * aka this is a kind of cache to avoid having to redetermine the available durability for each individual bit modified
     */
    public static class CalculatedInventory extends InventorySnapshot {
        private long durability, usedDurability;
        private Set<Integer> modifiedBitStorages = new HashSet<>();
        private Map<Integer, Integer> lastModifiedSlot = new HashMap<>(); //The last modified slot of the bit bag, this slot will be selected if possible.
        private int nextSelect = -1; //The slot to be selected next.
        private SelectedItemMode selectedMode = null; //The mode to be selected next.
        private HashMap<VoxelWrapper, Long> extracted = new HashMap<>();
        private boolean wastingBits = false, changed = true;

        public CalculatedInventory(ServerPlayerEntity player) {
            super(player);

            //Calculate durability
            for (ItemStack item : player.inventory.mainInventory) {
                if (item.getItem() instanceof ChiselItem)
                    durability += (item.getMaxDamage() - item.getDamage());
            }
            for (ItemStack item : player.inventory.offHandInventory) {
                if (item.getItem() instanceof ChiselItem)
                    durability += (item.getMaxDamage() - item.getDamage());
            }
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
            if(getPlayer().isCreative()) {
                usedDurability++; //for the statistics
                return true;
            }

            if(usedDurability >= durability) return false;
            usedDurability++;
            return true;
        }

        /**
         * Add ammount of material to be given back to the player.
         */
        public void addMaterial(VoxelWrapper w, long amount) {
            changed = true;
            if(w == null || VoxelType.isColoured(w.getId())) return; //Don't do coloured bits
            extracted.put(w, extracted.getOrDefault(w, 0L) + amount);
        }

        /**
         * Applies all changes to the real player's inventory.
         * Desyncs are ignored in the player's favor. (e.g. if the total durability decreased since the calculated inventory was made we don't mind the difference and take as much as available)
         */
        public void apply() {
            if(!getPlayer().isCreative()) //We don't need to take materials for creative players.
                addMaterial(bitPlaced, -bitsUsed);

            final NonNullList<ItemStack> inventory = getPlayer().inventory.mainInventory;

            applyDurabilityChanges();
            applyMaterialChanges();

            //Update selected slot without updating timestamp, "passive selection"
            SelectedItemMode m = ItemModeUtil.getGlobalSelectedItemMode(getPlayer());
            long t = ItemModeUtil.getHighestSelectionTimestamp(getPlayer());
            for(int i : lastModifiedSlot.keySet()) {
                if(!m.isNone() && ItemModeUtil.getSelectionTime(inventory.get(i)) == t) continue; //Don't change it for the currently selected one.
                int slot = lastModifiedSlot.get(i);
                final BitStorage bs = bitStorages.get(i);
                if(slot < 0 || slot > bs.getSlots()) continue; //Invalid slot
                ItemModeUtil.setMode(getPlayer(), inventory.get(i), SelectedItemMode.fromVoxelWrapper(bs.getSlotContent(slot)), false);
            }

            //Select the nextSelect if applicable
            if(nextSelect != -1)
                ItemModeUtil.setMode(getPlayer(), inventory.get(nextSelect), selectedMode, true);

            //Send updates for all modified bit bags
            for(int i : modifiedBitStorages) {
                if(i < 0) {
                    ItemModeUtil.updateStackCapability(inventory.get(-i), bitStorages.get(i), getPlayer());
                    continue;
                }
                ItemModeUtil.updateStackCapability(inventory.get(i), bitStorages.get(i), getPlayer());
            }
            modifiedBitStorages.clear();

            //Notify of bits wasted
            if(wastingBits)
                getPlayer().sendStatusMessage(new TranslationTextComponent("general."+ChiselsAndBits2.MOD_ID+".info.wasting_bits"), true);
        }

        /**
         * Removes materials from all bit storages in the inventory.
         */
        private void applyMaterialChanges() {
            //Timestamp for the truly selected bag
            long t = ItemModeUtil.getHighestSelectionTimestamp(getPlayer());
            SelectedItemMode m = ItemModeUtil.getGlobalSelectedItemMode(getPlayer());

            for(VoxelWrapper w : extracted.keySet()) {
                long added = extracted.get(w);
                if(added == 0) continue; //We don't need do nothing here.

                //can stop is used to determine if we're done with our problem
                Function<Long, Boolean> canStop = added < 0 ? ((i) -> i >= 0) : ((i) -> i <= 0);

                //First try to do/take from the currently selected bag first
                int i = ItemModeUtil.getGlobalSelectedItemSlot(getPlayer());
                added = addToStorage(i, w, added, t, m, true);

                //Now try to take from any random other bit storage that contains it
                for(int slot : bitStorages.keySet()) {
                    if(i == slot) continue;
                    if(canStop.apply(added)) break; //If we no longer have a problem we're done.
                    added = addToStorage(slot, w, added, t, m,true);
                }

                //For adding we also try storages that don't contain it
                if(added > 0) {
                    for(int slot : bitStorages.keySet()) {
                        if(canStop.apply(added)) break; //If we no longer have a problem we're done.
                        added = addToStorage(slot, w, added, t, m,false);
                    }
                }

                //If we still have stuff to add we are wasting bits, notify the user!
                if(added > 0) wastingBits = true;
            }
        }

        //Interal method for modifying storage contents for a given storage in a given slot.
        private long addToStorage(int slot, VoxelWrapper w, long added, long t, SelectedItemMode m, boolean requireContains) {
            if(!bitStorages.containsKey(slot)) return added; //If invalid we return instantly
            BitStorage bs = bitStorages.get(slot);
            if(requireContains && !bs.has(w)) return added; //If we can't take any more items we stop
            long capacity = added < 0 ? Math.max(added, -bs.get(bitPlaced)) : Math.min(added, bs.queryRoom(w));
            bs.add(w, capacity);
            //If adding select this slot passively
            if(capacity > 0) lastModifiedSlot.put(slot, bs.findSlot(w));
            modifiedBitStorages.add(slot);
            //Always lower added
            added += capacity > 0 ? -capacity : capacity;

            //If this is the currently selected one, keep the selection going by re-selecting this bit type elsewhere.
            //Also make sure this is the type currently being placed!
            if(bs.get(w) <= 0 && !m.isNone() && w.equals(m.getVoxelWrapper()) && ItemModeUtil.getSelectionTime(getPlayer().inventory.mainInventory.get(slot)) == t) {
                for(int otherSlot : bitStorages.keySet()) {
                    BitStorage otherBs = bitStorages.get(slot);
                    //Does this one have this bit?
                    if(otherBs.get(w) > 0) {
                        //Select the other bag now
                        nextSelect = otherSlot;
                        selectedMode = SelectedItemMode.fromVoxelWrapper(w);
                        break;
                    }
                }
            }
            return added;
        }

        /**
         * Applies durability damage to the chisels in the inventory of the player.
         */
        private void applyDurabilityChanges() {
            ItemStack selectedItem = getPlayer().getHeldItemMainhand();
            if (usedDurability > 0) {
                //Update the bits chiseled statistic
                getPlayer().addStat(ChiselsAndBits2.getInstance().getStatistics().BITS_CHISELED, (int) usedDurability);

                if (getPlayer().isCreative()) return; //We don't need to do the rest for creative players.

                boolean holdingChisel = selectedItem.getItem() instanceof ChiselItem;
                int targetSlot = getPlayer().inventory.currentItem;
                if (!holdingChisel) targetSlot = findNextChisel(-1);
                if (targetSlot == -1)
                    return; //This shouldn't happen, but it might, anyways we don't care for a little bit of durability theft so we don't need to force take durability.

                //We store the previous chisel's mode here to keep the mode through chisel changes. (just a lil quality 'o life)
                IItemMode oldMode = null;
                //Now we start taking durability from the chisel in our targetSlot and otherwise scan for other chisels to move to our targetSlot.
                while (usedDurability > 0) {
                    final PlayerInventory inventory = getPlayer().inventory;
                    ItemStack target = inventory.getStackInSlot(targetSlot);

                    //Check if the item is valid, otherwise move a chisel in from elsewhere.
                    if (target.isEmpty() || !(target.getItem() instanceof ChiselItem)) {
                        //Find a new chisel to move to the targetSlot
                        int foundChisel = findNextChisel(targetSlot);
                        if (foundChisel == -1)
                            return; //If we can't find a chisel we ignore the remaining durability debt.

                        ItemStack newChisel = inventory.getStackInSlot(foundChisel);
                        if (oldMode != null)
                            ItemModeUtil.setMode(getPlayer(), newChisel, oldMode, true);
                        inventory.removeStackFromSlot(foundChisel);
                        inventory.setInventorySlotContents(targetSlot, newChisel);
                        if (!target.isEmpty())
                            //If the target is somehow a valid item, try to get it back into the inventory, we don't care if this fails. Target should never not be empty.
                            inventory.addItemStackToInventory(target);

                        target = inventory.getStackInSlot(targetSlot);
                    }

                    //Prevent unnecessary mode fetching.
                    oldMode = !(target.getItem() instanceof IItemMenu) ? oldMode : ItemModeUtil.getItemMode(target);
                    //Get the maximum we can take from this chisel.
                    long capacity = Math.min(usedDurability, target.getMaxDamage() - target.getDamage());
                    //Take durability from the current item.
                    target.damageItem((int) capacity, getPlayer(), (p) -> p.sendBreakAnimation(Hand.MAIN_HAND));
                    usedDurability -= capacity;
                }
            }
        }

        /**
         * Finds the first slot a chisel is in.
         * @param exclude A specific slot to be excluded from the search.
         * @return -1 if no chisel can be found.
         */
        public int findNextChisel(int exclude) {
            for(int i = 0; i < getPlayer().inventory.mainInventory.size(); i++) {
                if(i == exclude) continue;
                if(getPlayer().inventory.mainInventory.get(i).getItem() instanceof ChiselItem)
                    return i;
            }
            return -1;
        }

        /**
         * Plays the correct sound effect when updating a chiseled block but only if the block was actually modified.
         */
        public void playSound(final World world, final BlockPos pos) {
            if(!changed) return;
            SoundType st = world.getBlockState(pos).getSoundType();
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, st.getBreakSound(), SoundCategory.BLOCKS, (st.getVolume() + 1.0F) / 16.0F, st.getPitch() * 0.9F);
        }

        /**
         * Removes a bit from the voxel blob and does all necessary surrounding logic.
         * @return true If the while loop can be broken. As soon as one true is returned false will never again be returned.
         */
        public boolean removeBit(final VoxelBlob vb, final int x, final int y, final int z) {
            final int blk = vb.get(x, y, z);
            if (blk == VoxelBlob.AIR_BIT)
                return false; //We don't need to remove air bits.

            if (damageChisel()) {
                vb.clear(x, y, z);

                addMaterial(VoxelWrapper.forAbstract(blk), 1);
            } else return true; //If damage chisel is false once it will never become true again, so this break saves time.

            return false;
        }

        /**
         * Places a bit of a given type in a given spot on the voxel blob, automatically handles using durability/extracting.
         * @return Can return various status codes with different meaning:
         *          0 - peaceful return, nothing was changed but there's no problem
         *          1 - chisel didn't have durability
         *          2 - didn't have materials to do placement
         */
        public int placeBit(final VoxelBlob vb, final int x, final int y, final int z, final BitOperation operation, final int placedBit) {
            final int blk = vb.get(x, y, z);

            //If this is a place operation we only place in air.
            if (operation == PLACE && blk != VoxelBlob.AIR_BIT && !VoxelType.isFluid(blk))
                return 0;

            //Don't waste durability on the same type.
            if (operation == SWAP && blk == placedBit)
                return 0;

            if (!hasMaterial()) return 2;
            if (damageChisel()) {
                //These two ifs need to be separate so one can fail but the other doesn't have to.
                //However we check hasMaterial early to ensure that this one will succeed true to avoid damage loss.
                if(canPlaceBit()) {
                    vb.set(x, y, z, placedBit);

                    //Track how many bits we've extracted and it's not a coloured bit.
                    if (operation == SWAP)
                        addMaterial(VoxelWrapper.forAbstract(blk), 1);
                } else return 2; //If can place bit is false once it will never become true again, so this break saves time.
            } else return 1; //If damage chisel is false once it will never become true again, so this break saves time.

            return 0;
        }

        @Override
        public ServerPlayerEntity getPlayer() {
            return (ServerPlayerEntity) super.getPlayer();
        }
    }
}
