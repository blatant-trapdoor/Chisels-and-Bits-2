package nl.dgoossens.chiselsandbits2.api.item.attributes;

import nl.dgoossens.chiselsandbits2.api.item.property.IItemProperty;

/**
 * Represents an item that owns a property stored in its NBT.
 * List of properties C&B has:
 *  - Item Mode
 *  - Selected Bit Type (VoxelWrapper)
 *  - Locked State
 *  - Colour
 *  - Placement/Swap
 *
 *  We use a slot system to avoid having a Set of IItemProperties.
 *  This should have also been an abstract class but we don't have that luxury.
 */
public interface IPropertyOwner {
    /**
     * Adds a new property to this item, should be called in the
     * constructor.
     * @return the slot id for this property
     */
    public default int addProperty(IItemProperty property) {
        return 0;
    }

    /**
     * Get the property in a given slot.
     * @param returnType Optional class to set return type argument.
     */
    public default <T> IItemProperty<T> getProperty(int slot, Class<T> returnType) {
        return null;
    }
}
