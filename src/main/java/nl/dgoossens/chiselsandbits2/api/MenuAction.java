package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;

/**
 * An enum representing the various actions that can be activated
 * through buttons in the radial menu's. Also occasionally used as
 * a reference to a given colour. (as the tint values are also stored here)
 */
public enum MenuAction {
    PLACE,
    SWAP,

    UNDO,
    REDO,

    ROLL_X,
    ROLL_Y,
    ROLL_Z,

    WHITE(DyeColor.WHITE, 16383998),
    BLACK(DyeColor.BLACK, 1908001),
    CYAN(DyeColor.CYAN, 1481884),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, 3847130),
    YELLOW(DyeColor.YELLOW, 16701501),
    PINK(DyeColor.PINK, 15961002),
    GRAY(DyeColor.GRAY, 4673362),
    BROWN(DyeColor.BROWN, 8606770),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY, 10329495),
    RED(DyeColor.RED, 11546150),
    MAGENTA(DyeColor.MAGENTA, 13061821),
    ORANGE(DyeColor.ORANGE, 16351261),
    LIME(DyeColor.LIME, 8439583),
    PURPLE(DyeColor.PURPLE, 8991416),
    BLUE(DyeColor.BLUE, 3949738),
    GREEN(DyeColor.GREEN, 6192150);

    private DyeColor dyeColour;
    private int colour;

    MenuAction() {
    }

    MenuAction(DyeColor dc, int col) {
        dyeColour = dc;
        colour = col;
    }

    public DyeColor getDyeColour() {
        return dyeColour;
    }

    public int getColour() {
        return colour;
    }

    public boolean hasIcon() {
        switch(this) {
            case PLACE:
            case SWAP:
            case UNDO:
            case REDO:
            case ROLL_X:
            case ROLL_Y:
            case ROLL_Z:
                return true;
            default:
                return false;
        }
    }

    /**
     * Get the localized key from this Menu Action.
     */
    public String getLocalizedName() {
        return I18n.format("general.chiselsandbits2.menuaction." + name().toLowerCase());
    }

    /**
     * Does this menu action have a hotkey?
     * (tape measure colours do not have hotkeys)
     */
    public boolean hasHotkey() {
        return dyeColour == null;
    }

    /**
     * Get the item mode type associated with this menu option, if any.
     */
    public ItemModeType getAssociatedType() {
        switch (this) {
            case ROLL_X:
            case ROLL_Y:
            case ROLL_Z:
                return ItemModeType.PATTERN;
            case PLACE:
            case SWAP:
                return ItemModeType.CHISEL;
            case UNDO:
            case REDO:
                return null; //Undo/redo are universal.
            default:
                return ItemModeType.TAPEMEASURE; //For all the colours
        }
    }
}
