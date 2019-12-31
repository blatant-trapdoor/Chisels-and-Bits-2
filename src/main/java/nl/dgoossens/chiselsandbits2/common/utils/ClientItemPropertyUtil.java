package nl.dgoossens.chiselsandbits2.common.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;
import nl.dgoossens.chiselsandbits2.api.item.DyedItemColour;
import nl.dgoossens.chiselsandbits2.api.item.IItemMode;
import nl.dgoossens.chiselsandbits2.common.impl.ItemMode;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

/**
 * Handles client-only parts of the ItemPropertyUtil.
 */
@OnlyIn(Dist.CLIENT)
public class ClientItemPropertyUtil {
    private static ItemMode globalCBM = (ItemMode) ItemModeType.CHISELED_BLOCK.getDefault();

    /**
     *  Get the chiseled block mode the main client player is using.
     */
    public static ItemMode getGlobalCBM() {
        return globalCBM;
    }

    /**
     * Set the globally selected chiseled block mode.
     */
    public static void setGlobalCBM(final ItemMode itemMode) {
        globalCBM = itemMode;
    }

    /**
     * Set a tape measure you're holding's colour to the passed colour.
     */
    public static void setTapeMeasureColor(DyedItemColour c) {

    }

    /**
     * Set the item state of an item to this value.
     * Works for:
     * - Morphing Bit Lock
     * - Chisel PLace/Swap
     */
    public static void setItemState(boolean b) {

    }

    /**
     * Set the main item mode of an itemstack.
     *
     public static void changeItemMode(final PlayerEntity player, final ItemStack item, final IItemMode newMode) {
     if(newMode instanceof SelectedItemMode && !(item.getItem() instanceof StorageItem)) {
     throw new RuntimeException("Can't set mode of item stack to selected item mode if item is not a storage item.");
     }

     final CSetItemModePacket packet = new CSetItemModePacket(newMode);
     ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(packet);

     //Update stack on client
     setMode(player, item, newMode, !SelectedItemMode.isNone(newMode)); //Don't update timestamp if this is empty.
     if(newMode.getType() == ItemModeType.CHISELED_BLOCK)
     ChiselsAndBits2.getInstance().getClient().resetPlacementGhost();

     //Show item mode change in hotbar
     if(packet.isValid(player))
     ClientUtil.reshowHighlightedStack();

     if(newMode instanceof SelectedItemMode)
     selected.remove(player.getUniqueID());
     }*/

    /**
     * Set the menu action mode of an itemstack.
     * MenuAction#COLOURS and MenuAction#PLACE/MenuAction#SWAP
     * are accepted.
     *
     public static void changeMenuActionMode(final IMenuAction newAction) {
     final CSetMenuActionModePacket packet = new CSetMenuActionModePacket(newAction);
     ChiselsAndBits2.getInstance().getNetworkRouter().sendToServer(packet);

     //Show item mode change in hotbar
     if(packet.isValid(Minecraft.getInstance().player))
     ClientUtil.reshowHighlightedStack();
     }*/

    /**
     * Reshows the highlighted stack item.
     * Only works on client-side.
     */
    protected static void reshowHighlightedStack() {
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
}
