package nl.dgoossens.chiselsandbits2.common.registry;

/**
 * The different colours that bags and beakers can be coloured to look like.
 */
public enum BagBeakerColours {
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
    BagBeakerColours(int col) {
        colour = col;
    }

    public int getColour() {
        return colour;
    }
}
