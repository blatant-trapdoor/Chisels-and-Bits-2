package nl.dgoossens.chiselsandbits2.api.modes;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.DyeColor;

public enum MenuAction {
    UNDO,
    REDO,

    ROLL_X,
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
    GREEN(DyeColor.GREEN, 6192150)
    ;

    private DyeColor dyeColour;
    private int colour;
    MenuAction() {}
    MenuAction(DyeColor dc, int col) { dyeColour=dc; colour=col; }
    public DyeColor getDyeColour() { return dyeColour; }
    public int getColour() { return colour; }

    /**
     * Get the localized key from this Menu Action.
     */
    public String getLocalizedName() {
        return I18n.format("general.chiselsandbits2.menuaction."+name().toLowerCase());
    }
}
