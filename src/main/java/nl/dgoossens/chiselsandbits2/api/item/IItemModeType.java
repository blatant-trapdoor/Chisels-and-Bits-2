package nl.dgoossens.chiselsandbits2.api.item;

import net.minecraft.item.ItemStack;
import nl.dgoossens.chiselsandbits2.ChiselsAndBits2;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Generic interface for a type of item mode.
 * Register this mode type using {@link ItemPropertyAPI#registerModeType(IItemModeType)}
 */
public interface IItemModeType {
    /**
     * Class implementing IItemModeType should be an enum.
     */
    String name();

    /**
     * Get a list of item modes that are a part of this item mode type.
     */
    public default List<IItemMode> getItemModes(final ItemStack item) {
        return ChiselsAndBits2.getInstance().getAPI().getItemPropertyRegistry().getModes().stream()
                .filter(f -> f.getType() == this)
                .collect(Collectors.toList());
    }

    /**
     * Returns whether or not this type is "dynamic". Dynamic types are types where the item mode is an object and has more information
     * attached, static types are defined by the ItemMode enum.
     *
     * @deprecated Currently C&B2 doesn't support external dynamic mode types yet.
     */
    @Deprecated
    public default boolean isDynamic() {
        return false;
    }
}
