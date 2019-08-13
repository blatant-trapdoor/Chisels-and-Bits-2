package nl.dgoossens.chiselsandbits2.api.modes;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;

import java.util.stream.Stream;

/**
 * The current mode the item is using, shared between patterns and chisels.
 */
public enum ItemMode {
    CHISEL_SINGLE,
    CHISEL_SNAP2,
    CHISEL_SNAP4,
    CHISEL_SNAP8,
    CHISEL_LINE,
    CHISEL_PLANE,
    CHISEL_CONNECTED_PLANE,
    CHISEL_CUBE3,
    CHISEL_CUBE5,
    CHISEL_CUBE7,
    CHISEL_SAME_MATERIAL,
    CHISEL_DRAW_REGION,
    CHISEL_CONNECTED_MATERIAL,

    PATTERN_REPLACE,

    ;

    public static enum Type {
        CHISEL,
        PATTERN
    }

    /**
     * Get this item mode's type. (associated with name())
     */
    public Type getType() { return Stream.of(Type.values()).filter(f -> name().startsWith(f.name())).findAny().orElse(Type.CHISEL); }

    /**
     * Fetch the item mode associated with this item.
     */
    public static ItemMode getChiselMode(final ItemStack stack) {
        if(stack != null) {
            try {
                final CompoundNBT nbt = stack.getTag();
                if (nbt != null && nbt.contains( "mode" ))
                    return valueOf(nbt.getString( "mode" ));
            }
            catch ( final IllegalArgumentException iae ) {} //nope!
            catch ( final Exception e ) { e.printStackTrace(); }
        }

        return CHISEL_SINGLE;
    }

    /**
     * Set the mode of this itemstack to this enum value.
     */
    public void setMode(final ItemStack stack) {
        if (stack != null) stack.setTagInfo( "mode", new StringNBT( name() ) );
    }
}
