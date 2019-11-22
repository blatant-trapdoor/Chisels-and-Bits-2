package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.LongNBT;
import net.minecraft.nbt.StringNBT;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.*;
import nl.dgoossens.chiselsandbits2.common.bitstorage.BitStorageImpl;
import nl.dgoossens.chiselsandbits2.common.bitstorage.StorageCapabilityProvider;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.ChiselHandler;
import nl.dgoossens.chiselsandbits2.common.chiseledblock.voxel.VoxelBlob;
import nl.dgoossens.chiselsandbits2.common.items.*;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.common.network.client.CSetItemModePacket;
import nl.dgoossens.chiselsandbits2.common.network.client.CSetMenuActionModePacket;
import nl.dgoossens.chiselsandbits2.common.network.server.SSynchronizeBitStoragePacket;
import nl.dgoossens.chiselsandbits2.common.utils.ModUtil;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;

import static nl.dgoossens.chiselsandbits2.api.ItemMode.*;

/**
 * The manager of which mode a tool is currently using.
 */
public class ChiselModeManager {
    /**
     * Set the main item mode of an itemstack.
     */
    public static void changeItemMode(final ItemStack item, final IItemMode newMode) {
        if(newMode instanceof SelectedItemMode && !(item.getItem() instanceof StorageItem)) {
            throw new RuntimeException("Can't set mode of item stack to selected item mode if item is not a storage item.");
        }

        final CSetItemModePacket packet = new CSetItemModePacket(newMode);
        ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(packet);

        //Update stack on client
        setMode(Minecraft.getInstance().player, item, newMode);

        //Show item mode change in hotbar
        if(packet.isValid(Minecraft.getInstance().player))
            reshowHighlightedStack();

        if(newMode instanceof SelectedItemMode)
            selected.remove(Minecraft.getInstance().player.getUniqueID());
    }

    /**
     * Set the menu action mode of an itemstack.
     * MenuAction#COLOURS and MenuAction#PLACE/MenuAction#SWAP
     * are accepted.
     */
    public static void changeMenuActionMode(final MenuAction newAction) {
        final CSetMenuActionModePacket packet = new CSetMenuActionModePacket(newAction);
        ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(packet);

        //Show item mode change in hotbar
        if(packet.isValid(Minecraft.getInstance().player))
            reshowHighlightedStack();
    }

    /**
     * Reshows the highlighted stack item.
     */
    private static void reshowHighlightedStack() {
        try {
            IngameGui ig = Minecraft.getInstance().ingameGUI;
            //IngameGui#highlightingItemStack
            Field f = null;
            for(Field fe : IngameGui.class.getDeclaredFields()) {
                //We abuse the fact that IngameGui only has one ItemStack and that's the one we need.
                if(ItemStack.class.isAssignableFrom(fe.getType())) {
                    f = fe;
                    break;
                }
            }
            if(f == null) throw new RuntimeException("Unable to lookup textures.");
            f.setAccessible(true);
            f.set(ig, Minecraft.getInstance().player.getHeldItemMainhand());

            //IngameGui#remainingHighlightTicks
            Field f2 = null;
            int i = 0;
            for(Field fe : IngameGui.class.getDeclaredFields()) {
                //We want the third int type field which is remainingHighlightTicks.
                if(Integer.TYPE.isAssignableFrom(fe.getType())) {
                    i++;
                    if(i==3) {
                        f2 = fe;
                        break;
                    }
                }
            }
            f2.setAccessible(true);
            f2.set(ig, 40);
        } catch(Exception x) {
            x.printStackTrace();
        }
    }

    /**
     * Updates the stack capability from the server to the client.
     */
    public static void updateStackCapability(final ItemStack item, final BitStorage cap, final PlayerEntity player) {
        ChiselsAndBits2.getInstance().getNetworkRouter().sendTo(new SSynchronizeBitStoragePacket(cap, player.inventory.getSlotFor(item)), (ServerPlayerEntity) player);
        validateSelectedBitType(player, item);
    }

    private static void validateSelectedBitType(final PlayerEntity player, final ItemStack item) {
        //Check if selected type is no longer valid
        SelectedItemMode selected = getSelectedItem(item);
        if(selected == null) return;
        BitStorage storage = item.getCapability(StorageCapabilityProvider.STORAGE).orElse(null);
        if(storage == null) return;
        VoxelWrapper sel = selected.getVoxelWrapper();
        if(storage.get(sel) <= 0)
            setMode(player, item, SelectedItemMode.NONE);
    }

    /**
     * Scrolls through the current options and goes to the next one depending
     * on which direction was scrolled in.
     */
    public static void scrollOption(IItemMode currentMode, ItemStack item, final double dwheel) {
        if (!ChiselsAndBits2.getInstance().getConfig().enableModeScrolling.get()) return;
        if (currentMode instanceof ItemMode) {
            int offset = ((ItemMode) currentMode).ordinal();
            do {
                offset += (dwheel < 0 ? -1 : 1);
                if (offset >= values().length) offset = 0;
                if (offset < 0) offset = values().length - 1;
            } while (ItemMode.values()[offset].getType() != currentMode.getType());
            changeItemMode(item, ItemMode.values()[offset]);
        } else {
            IItemMode i = getMode(item);
            if(!(i instanceof SelectedItemMode)) return; //Just in case.
            SelectedItemMode current = ((SelectedItemMode) i);
            item.getCapability(StorageCapabilityProvider.STORAGE).ifPresent(bs -> {
                if(bs.getOccupiedSlotCount() <= 1) return; //You can't scroll without at least 2 elements.
                VoxelWrapper wrapper = current.getVoxelWrapper();
                int j = bs.getSlot(wrapper);
                j += (dwheel < 0 ? -1 : 1);
                if(bs.getOccupiedSlotCount() <= j) j = 0;
                if(j < 0) j = bs.getOccupiedSlotCount() - 1;
                changeItemMode(item, SelectedItemMode.fromVoxelWrapper(wrapper));
            });
        }
    }

    /**
     * Get the item mode being used by the item currently held by
     * the player.
     */
    public static IItemMode getMode(final PlayerEntity player) {
        final ItemStack ei = player.getHeldItemMainhand();
        return getMode(ei);
    }

    /**
     * Fetch the item mode associated with this item.
     * Type is required for the returned value when no
     * mode is found!
     */
    public static IItemMode getMode(final ItemStack stack) {
        //Prevent unnecessary resolving or random MALLET_UNKNOWN shenanigans.
        if(!(stack.getItem() instanceof IItemMenu)) return null;

        final CompoundNBT nbt = stack.getTag();
        if (nbt != null && nbt.contains("mode")) {
            try {
                return resolveMode(nbt.getString("mode"), nbt.getBoolean("isDynamic"), nbt.getInt("dynamicId"));
            } catch (final Exception x) {
                x.printStackTrace();
            }
        }

        return (stack.getItem() instanceof BitBagItem) ||
                (stack.getItem() instanceof BitBeakerItem) ||
                (stack.getItem() instanceof PaletteItem) ? SelectedItemMode.NONE :
                (stack.getItem() instanceof ChiselHandler.BitModifyItem) ? CHISEL_SINGLE :
                        (stack.getItem() instanceof PatternItem) ? PATTERN_REPLACE :
                                (stack.getItem() instanceof TapeMeasureItem) ? TAPEMEASURE_BIT :
                                        (stack.getItem() instanceof WrenchItem) ? WRENCH_ROTATE :
                                                (stack.getItem() instanceof BlueprintItem) ? BLUEPRINT_UNKNOWN :
                                                        MALLET_UNKNOWN;
    }

    /**
     * Get the selected item of an item stack, this item stack should be a BitStorage holder.
     */
    public static SelectedItemMode getSelectedItem(final ItemStack stack) {
        IItemMode ret = getMode(stack);
        if(ret instanceof SelectedItemMode)
            return (SelectedItemMode) ret;
        return null;
    }

    /**
     * If this stack is of a dynamic itemmode type this will return the timestamp when
     * the selection was made.
     * Used to determine what to place as the most recently selected item mode of a storage
     * item is used for placement regardless of the inventory slots.
     */
    public static long getSelectionTime(final ItemStack stack) {
        if(stack.getItem() instanceof IItemMenu) {
            final CompoundNBT nbt = stack.getTag();
            if (nbt != null && nbt.contains("timestamp"))
                return nbt.getLong("timestamp");
        }
        return 0L;
    }

    /**
     * Resolves an IItemMode from the output of
     * {@link IItemMode#getName()}
     */
    public static IItemMode resolveMode(final String name, final boolean isDynamic, final int dynamicId) {
        if(isDynamic) {
            //Dynamic Item Mode
            return name.equals(SelectedItemMode.NONE.getName()) ? SelectedItemMode.NONE : SelectedItemMode.fromVoxelType(dynamicId);
        } else {
            try {
                return ItemMode.valueOf(name);
            } catch (final IllegalArgumentException il) {}
        }
        return CHISEL_SINGLE;
    }

    /**
     * Set the mode of this itemstack to this enum value.
     */
    public static void setMode(final PlayerEntity player, final ItemStack stack, final IItemMode mode) {
        if (stack != null) {
            stack.setTagInfo("mode", new StringNBT(mode.getName()));
            stack.setTagInfo("isDynamic", new ByteNBT(mode.getType().isDynamic() ? (byte) 1 : (byte) 0));
            stack.setTagInfo("dynamicId", new IntNBT(mode.getDynamicId()));
            if(mode.getType().isDynamic())
                stack.setTagInfo("timestamp", new LongNBT(System.currentTimeMillis()));
        }

        selected.remove(player.getUniqueID());
    }

    /**
     * Force recalculates this client player's selected bit type.
     */
    public static void recalculateSelectedBit() {
        selected.remove(Minecraft.getInstance().player.getUniqueID());
    }

    /**
     * Fetch the menu action associated to this item,
     * can be a colour or place/replace.
     */
    public static MenuAction getMenuActionMode(final ItemStack stack) {
        try {
            final CompoundNBT nbt = stack.getTag();
            if (nbt != null && nbt.contains("menuAction"))
                return MenuAction.valueOf(nbt.getString("menuAction"));
        } catch (final IllegalArgumentException iae) { //nope!
        } catch (final Exception e) {
            e.printStackTrace();
        }

        return (stack.getItem() instanceof TapeMeasureItem) ? MenuAction.WHITE :
                MenuAction.PLACE;
    }

    /**
     * Set the menu action mode of this itemstack to this enum value.
     */
    public static void setMenuActionMode(final ItemStack stack, final MenuAction action) {
        if (stack != null) stack.setTagInfo("menuAction", new StringNBT(action.name()));
    }

    //--- SELECTED BIT ---
    private static Map<UUID, SelectedItemMode> selected = new HashMap<>();

    /**
     * Gets the currently selected bit type as a selected item mode.
     */
    public static SelectedItemMode getSelectedBitMode(final PlayerEntity player) {
        if(!selected.containsKey(player.getUniqueID())) {
            long stamp = 0;

            //Scan all storage containers for the most recently selected one.
            for (ItemStack item : player.inventory.mainInventory) {
                if (item.getItem() instanceof StorageItem) {
                    long l = ChiselModeManager.getSelectionTime(item);
                    if (l > stamp) {
                        stamp = l;
                        SelectedItemMode im = ChiselModeManager.getSelectedItem(item);
                        if(im != null && im.getBitId() != VoxelBlob.AIR_BIT)
                            selected.put(player.getUniqueID(), im);
                    }
                }
            }
            if(!selected.containsKey(player.getUniqueID()))
                selected.put(player.getUniqueID(), SelectedItemMode.NONE);
        }
        return selected.get(player.getUniqueID());
    }

    /**
     * Get the currently selected bit type.
     */
    public static int getSelectedBit(final PlayerEntity player) {
        return getSelectedBitMode(player).getBitId();
    }
}
