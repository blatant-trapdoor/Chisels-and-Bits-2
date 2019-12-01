package nl.dgoossens.chiselsandbits2.api;

import net.minecraft.client.resources.I18n;
import nl.dgoossens.chiselsandbits2.common.impl.ItemModeType;

/**
 * An enum representing the various actions that can be activated
 * through buttons in the radial menu's.
 */
public enum MenuAction {
    PLACE,
    SWAP,

    UNDO,
    REDO,

    ROLL_X,
    ROLL_Z,

    WHITE(16383998),
    BLACK(1908001),
    CYAN(1481884),
    LIGHT_BLUE(3847130),
    YELLOW(16701501),
    PINK(15961002),
    GRAY(4673362),
    BROWN(8606770),
    LIGHT_GRAY(10329495),
    RED(11546150),
    MAGENTA(13061821),
    ORANGE(16351261),
    LIME(8439583),
    PURPLE(8991416),
    BLUE(3949738),
    GREEN(6192150);

    private int colour = 0;

    MenuAction() {
    }

    MenuAction(int col) {
        colour = col;
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
        return colour == 0;
    }

    /**
     * Get the item mode type associated with this menu option, if any.
     */
    public IItemModeType getAssociatedType() {
        switch (this) {
            case ROLL_X:
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
