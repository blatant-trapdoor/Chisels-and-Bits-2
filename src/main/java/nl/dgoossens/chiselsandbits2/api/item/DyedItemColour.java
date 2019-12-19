package nl.dgoossens.chiselsandbits2.api.item;

import net.minecraft.item.DyeColor;

/**
 * The different colours that IColourable items can be coloured to look like.
 */
public enum DyedItemColour {
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
    GREEN(6192150),
    ;

    private int colour;
    DyedItemColour(int col) {
        colour = col;
    }

    public int getColour() {
        return colour;
    }

    public static DyedItemColour fromDye(DyeColor dye) {
        switch(dye) {
            case WHITE: return WHITE;
            case ORANGE: return ORANGE;
            case MAGENTA: return MAGENTA;
            case LIGHT_BLUE: return LIGHT_BLUE;
            case YELLOW: return YELLOW;
            case LIME: return LIME;
            case PINK: return PINK;
            case GRAY: return GRAY;
            case LIGHT_GRAY: return LIGHT_GRAY;
            case CYAN: return CYAN;
            case PURPLE: return PURPLE;
            case BLUE: return BLUE;
            case BROWN: return BROWN;
            case GREEN: return GREEN;
            case RED: return RED;
            case BLACK: return BLACK;
        }
        return BLACK;
    }
}
