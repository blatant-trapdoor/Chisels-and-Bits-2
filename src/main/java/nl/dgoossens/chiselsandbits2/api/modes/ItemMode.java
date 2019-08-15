package nl.dgoossens.chiselsandbits2.api.modes;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import nl.dgoossens.chiselsandbits2.common.items.ChiselItem;
import nl.dgoossens.chiselsandbits2.common.items.PatternItem;
import nl.dgoossens.chiselsandbits2.common.items.TapeMeasureItem;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The current mode the item is using, shared between patterns and chisels.
 */
public enum ItemMode {
    CHISEL_SINGLE,
    CHISEL_LINE,
    CHISEL_PLANE,
    CHISEL_CONNECTED_PLANE,
    CHISEL_CONNECTED_MATERIAL,
    CHISEL_DRAWN_REGION,
    CHISEL_SAME_MATERIAL,
    CHISEL_SNAP8,
    CHISEL_SNAP4,
    CHISEL_SNAP2,
    CHISEL_CUBE3,
    CHISEL_CUBE5,
    CHISEL_CUBE7,

    PATTERN_REPLACE, //I've actually rarely used patterns myself so I'll go experiment with them once I get around to them, then I'll probably add some more modes.
    PATTERN_ADDITIVE,
    PATTERN_REPLACEMENT,
    PATTERN_IMPOSE,

    TAPEMEASURE_BIT,
    TAPEMEASURE_BLOCK,
    TAPEMEASURE_DISTANCE,

    WRENCH_ROTATE,
    WRENCH_NUDGE_BIT,
    WRENCH_NUDGE_BLOCK,
    ;

    /**
     * Get the localized key from this Item Mode.
     */
    public String getLocalizedName() {
        return I18n.format("general.chiselsandbits2.itemmode."+getTypelessName());
    }

    /**
     * Return this enum's name() but without the type in front.
     */
    public String getTypelessName() {
        return name().substring(getType().name().length()+1).toLowerCase();
    }

    public static enum Type {
        //Type names must be identical to the startsWith() of the ItemMode!
        CHISEL,
        PATTERN,
        TAPEMEASURE,
        WRENCH,
        ;

        /**
         * Get all item modes associated with this type.
         */
        public Set<ItemMode> getItemModes() {
            return Stream.of(ItemMode.values()).filter(f -> f.name().startsWith(name())).collect(Collectors.toSet());
        }

        /**
         * Get all item modes associated with this type.
         * Keep them sorted the same way they are in the enum is.
         */
        public List<ItemMode> getSortedItemModes() {
            return Stream.of(ItemMode.values()).filter(f -> f.name().startsWith(name())).collect(Collectors.toList());
        }
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
    public static ItemMode getMode(final ItemStack stack) {
        try {
            final CompoundNBT nbt = stack.getTag();
            if (nbt != null && nbt.contains( "mode" ))
                return valueOf(nbt.getString( "mode" ));
        } catch ( final IllegalArgumentException iae ) { //nope!
        } catch ( final Exception e ) { e.printStackTrace(); }

        return (stack.getItem() instanceof ChiselItem) ? CHISEL_SINGLE :
               (stack.getItem() instanceof PatternItem) ? PATTERN_REPLACE :
               (stack.getItem() instanceof TapeMeasureItem) ? TAPEMEASURE_BIT :
               WRENCH_ROTATE;
    }

    /**
     * Set the mode of this itemstack to this enum value.
     */
    public void setMode(final ItemStack stack) {
        if (stack != null) stack.setTagInfo( "mode", new StringNBT( name() ) );
    }

    /**
     * Fetch the colour associated with this item. The color
     * can be changed using MenuAction's.
     */
    public static MenuAction getColour(final ItemStack stack) {
        try {
            final CompoundNBT nbt = stack.getTag();
            if (nbt != null && nbt.contains( "colour" ))
                return MenuAction.valueOf(nbt.getString( "colour" ));
        } catch ( final IllegalArgumentException iae ) { //nope!
        } catch ( final Exception e ) { e.printStackTrace(); }

        return MenuAction.WHITE;
    }

    /**
     * Set the mode of this itemstack to this enum value.
     */
    public static void setColour(final ItemStack stack, final DyeColor color) {
        if (stack != null) stack.setTagInfo( "colour", new StringNBT( color.name() ) );
    }
}
