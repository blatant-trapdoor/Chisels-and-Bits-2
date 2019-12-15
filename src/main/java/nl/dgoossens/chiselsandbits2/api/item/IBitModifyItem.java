package nl.dgoossens.chiselsandbits2.api.item;

/**
 * Interface for any item that is capable of modifying bits in the world.
 */
public interface IBitModifyItem {
    /**
     * Whether or not this item can be used to perform a given modification type.
     */
    boolean canPerformModification(final ModificationType type);

    /**
     * The various types of modifications that can be applied.
     */
    static enum ModificationType {
        EXTRACT, //Remove bits
        BUILD, //Place bits

        CUSTOM, //Unknown type of action, use an interface extending IBitModifyItem and specify it
    }
}
