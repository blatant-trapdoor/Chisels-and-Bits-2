package nl.dgoossens.chiselsandbits2.common.impl;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.IItemMode;
import nl.dgoossens.chiselsandbits2.api.ItemMode;
import nl.dgoossens.chiselsandbits2.api.MenuAction;
import nl.dgoossens.chiselsandbits2.api.SelectedItemMode;
import nl.dgoossens.chiselsandbits2.common.items.*;
import nl.dgoossens.chiselsandbits2.common.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.common.network.packets.PacketSetItemMode;
import nl.dgoossens.chiselsandbits2.common.network.packets.PacketSetMenuActionMode;

import java.awt.*;

import static nl.dgoossens.chiselsandbits2.api.ItemMode.*;

/**
 * The manager of which mode a tool is currently using.
 */
public class ChiselModeManager {
    /**
     * Set the main item mode of an itemstack.
     */
    public static void changeItemMode(final IItemMode newMode) {
        final PacketSetItemMode packet = new PacketSetItemMode(newMode);
        NetworkRouter.sendToServer(packet);
    }

    /**
     * Set the menu action mode of an itemstack.
     * MenuAction#COLOURS and MenuAction#PLACE/MenuAction#REPLACE
     * are accepted.
     */
    public static void changeMenuActionMode(final MenuAction newAction) {
        final PacketSetMenuActionMode packet = new PacketSetMenuActionMode(newAction);
        NetworkRouter.sendToServer(packet);
    }

    /**
     * Scrolls through the current options and goes to the next one depending
     * on which direction was scrolled in.
     */
    public static void scrollOption(IItemMode currentMode, final double dwheel) {
        if (!ChiselsAndBits2.getInstance().getConfig().enableModeScrolling.get()) return;
        if (currentMode instanceof ItemMode) {
            int offset = ((ItemMode) currentMode).ordinal();
            do {
                offset += (dwheel < 0 ? -1 : 1);
                if (offset >= values().length) offset = 0;
                if (offset < 0) offset = values().length - 1;
            } while (ItemMode.values()[offset].getType() != currentMode.getType());
            changeItemMode(ItemMode.values()[offset]);
        } else {
            //TODO implement bit bag scroll
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
        final CompoundNBT nbt = stack.getTag();
        if (nbt != null && nbt.contains("mode")) {
            try {
                return resolveMode(nbt.getString("mode"), stack);
            } catch (final Exception x) {
                x.printStackTrace();
            }
        }

        return (stack.getItem() instanceof BitBagItem) ? SelectedItemMode.NONE_BAG :
                (stack.getItem() instanceof BitBeakerItem) ? SelectedItemMode.NONE_BEAKER :
                        (stack.getItem() instanceof PaletteItem) ? SelectedItemMode.NONE_BOOKMARK :
                                (stack.getItem() instanceof ChiselItem) ? CHISEL_SINGLE :
                                        (stack.getItem() instanceof PatternItem) ? PATTERN_REPLACE :
                                                (stack.getItem() instanceof TapeMeasureItem) ? TAPEMEASURE_BIT :
                                                        (stack.getItem() instanceof WrenchItem) ? WRENCH_ROTATE :
                                                                (stack.getItem() instanceof BlueprintItem) ? BLUEPRINT_UNKNOWN :
                                                                        MALLET_UNKNOWN;
    }

    /**
     * Resolves an IItemMode from the output of
     * {@link IItemMode#getName()}
     */
    public static IItemMode resolveMode(final String name, final ItemStack item) {
        try {
            return ItemMode.valueOf(name);
        } catch (final IllegalArgumentException il) {
            return item.getItem() instanceof PaletteItem ? SelectedItemMode.fromColour(new Color(Integer.valueOf(name), true)) : SelectedItemMode.fromName(name, item.getItem() instanceof BitBeakerItem);
        }
    }

    /**
     * Set the mode of this itemstack to this enum value.
     */
    public static void setMode(final ItemStack stack, final IItemMode mode) {
        if (stack != null) stack.setTagInfo("mode", new StringNBT(mode.getName()));
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
}
