package nl.dgoossens.chiselsandbits2.api.modes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.network.NetworkRouter;
import nl.dgoossens.chiselsandbits2.network.packets.PacketSetChiselMode;

public class ChiselModeManager {
    public static void changeChiselMode(
            final ItemMode newMode)
    {
        final PacketSetChiselMode packet = new PacketSetChiselMode(newMode);
        NetworkRouter.sendToServer( packet );
    }

    public static void scrollOption(
            final ItemMode.Type tool,
            ItemMode currentMode,
            final int dwheel )
    {
        int offset = currentMode.ordinal();
        do {
            offset = testOffset(offset + ( dwheel < 0 ? -1 : 1 ));
            currentMode = ItemMode.values()[offset];
        } while(currentMode.getType()!=tool);

        changeChiselMode( currentMode );
    }

    private static int testOffset(int offset) {
        if ( offset >= ItemMode.values().length )
        {
            return 0;
        }

        if ( offset < 0 )
        {
            return ItemMode.values().length - 1;
        }
        return offset;
    }

    public static ItemMode getMode(
            final PlayerEntity player )
    {
        final ItemStack ei = player.getHeldItemMainhand();
        if ( ei != null && ei.getItem() instanceof ChiselItem)
        {
            return ItemMode.getChiselMode( ei );
        }
        return ItemMode.CHISEL_SINGLE;
    }
}
