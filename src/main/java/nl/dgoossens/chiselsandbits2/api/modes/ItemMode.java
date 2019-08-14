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

    PATTERN_REPLACE, //DG: I've actually rarely used patterns myself so I'll go experiment with them once I get around to them, then I'll probably add some more modes.
    PATTERN_ADDITIVE,
    PATTERN_REPLACENT,
    PATTERN_IMPOSE,

    TAPEMEASURE_BIT,
    TAPEMEASURE_BLOCK,
    TAPEMEASURE_DISTANCE,
    ;

    public static enum Type {
        //Type names must be identical to the startsWith() of the ItemMode!
        CHISEL,
        PATTERN,
        TAPEMEASURE
    }

    /**
     * Get this item mode's type. (associated with name())
     */
    public Type getType() { return Stream.of(Type.values()).filter(f -> name().startsWith(f.name())).findAny().orElse(Type.CHISEL); }

    /**
     * Fetch the item mode associated with this item.
     * Type is required for the returned value when no
     * mode is found!
     */
    public static ItemMode getMode(final Type type, final ItemStack stack) {
        if(stack != null) {
            try {
                final CompoundNBT nbt = stack.getTag();
                if (nbt != null && nbt.contains( "mode" ))
                    return valueOf(nbt.getString( "mode" ));
            }
            catch ( final IllegalArgumentException iae ) {} //nope!
            catch ( final Exception e ) { e.printStackTrace(); }
        }

        return type == Type.CHISEL ? CHISEL_SINGLE : type == Type.PATTERN ? PATTERN_REPLACE : TAPEMEASURE_BIT;
    }

    /**
     * Set the mode of this itemstack to this enum value.
     */
    public void setMode(final ItemStack stack) {
        if (stack != null) stack.setTagInfo( "mode", new StringNBT( name() ) );
    }
}
