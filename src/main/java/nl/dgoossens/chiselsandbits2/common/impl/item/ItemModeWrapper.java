package nl.dgoossens.chiselsandbits2.common.impl.item;

public class ItemModeWrapper {
    private ItemMode content = (ItemMode) ItemModeType.CHISELED_BLOCK.getDefault();

    /**
     * Get the content of this item mode wrapper.
     */
    public ItemMode get() {
        return content;
    }

    /**
     * Modify the contents of this wrapper.
     */
    public void insert(final ItemMode mode) {
        content = mode;
    }
}
