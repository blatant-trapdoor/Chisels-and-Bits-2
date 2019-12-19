package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfiguration {
    public final ForgeConfigSpec SERVER;
    public final ForgeConfigSpec CLIENT;

    // --- CLIENT VALUES ---
    public ForgeConfigSpec.IntValue maxMillisecondsUploadingPerFrame;
    public ForgeConfigSpec.IntValue dynamicMaxConcurrentTesselators;

    public ForgeConfigSpec.DoubleValue radialMenuVolume;
    public ForgeConfigSpec.BooleanValue enableToolbarIcons;
    public ForgeConfigSpec.BooleanValue enableModeScrolling;
    public ForgeConfigSpec.BooleanValue enablePlacementGhost;

    public ForgeConfigSpec.BooleanValue enableVivecraftCompatibility;
    public ForgeConfigSpec.BooleanValue disableUnfinishedFeatures;

    // --- SERVER VALUES ---
    public ForgeConfigSpec.DoubleValue maxDrawnRegionSize;
    public ForgeConfigSpec.IntValue maxUndoLevel;

    public ForgeConfigSpec.IntValue tapeMeasureLimit;
    public ForgeConfigSpec.IntValue chiselDurability;
    public ForgeConfigSpec.IntValue typeSlotsPerBag;
    public ForgeConfigSpec.IntValue typeSlotsPerBeaker;
    public ForgeConfigSpec.IntValue bookmarksPerPalette;
    public ForgeConfigSpec.LongValue bitsPerTypeSlot;


    public ForgeConfigSpec.BooleanValue showBitsAvailableAsDurability;

    /**
     * Initialise all configuration values.
     */
    public ModConfiguration() {
        //CLIENT
        {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.comment("General Settings");
            builder.push("general");

            radialMenuVolume = builder
                    .comment("How loud should the radial menu sound be?")
                    .defineInRange("radialMenuVolume", 0.15, 0.0, 2.0);

            enableToolbarIcons = builder
                    .comment("Enables selection ghosts in the toolbar next to your tool item")
                    .define("enableToolbarIcons", true);

            enableModeScrolling = builder
                    .comment("When enabled the mode of a tool will be changed when using the scroll wheel whilst shifting and holding the item")
                    .define("enableModeScrolling", true);

            enablePlacementGhost = builder
                    .comment("Disable to stop ghost preview blocks from showing whilst holding a chiseled block.")
                    .define("enablePlacementGhost", true);

            builder.pop();

            builder.comment("Performance Settings");
            builder.push("performance");

            maxMillisecondsUploadingPerFrame = builder
                    .comment("How many milliseconds the client can spend uploading models each frame")
                    .defineInRange("maxMillisecondsUploadingPerFrame", 15, 1, 1000);

            dynamicMaxConcurrentTesselators = builder
                    .comment("How many block models can fromName rendered at the same time. This will automatically be set to 2 if less than 1256 MB in memory is detected.")
                    .defineInRange("dynamicMaxConcurrentTesselators", 32, 1, 256);

            builder.pop();

            builder.comment("Integration Settings");
            builder.push("integration");

            enableVivecraftCompatibility = builder
                    .comment("Turn on compatibility with Vivecraft, this turns the radial menu into an actual GUI instead of an overlay that can only be closed by clicking an option.")
                    .define("enableVivecraftCompatibility", false);

            builder.pop();

            builder.comment("Debug Settings");
            builder.push("debug");

            disableUnfinishedFeatures = builder
                    .comment("If false will enable all unfinished items and features. This setting is temporary and will be removed after the alpha releases of C&B2 have ended.")
                    .define("disableUnfinishedFeatures", true);

            builder.pop();

            CLIENT = builder.build();
        }

        //SERVER
        {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.comment("General Settings");
            builder.push("general");

            tapeMeasureLimit = builder
                    .comment("The maximum of measurements of the tape measure you can have at once.")
                    .defineInRange("tapeMeasureLimit", 10, 1, Integer.MAX_VALUE);

            maxDrawnRegionSize = builder
                    .comment("At how many blocks the width/length of the drawn region selection should be capped")
                    .defineInRange("maxDrawnRegionSize", 4.0, 1.0, 16.0);

            maxUndoLevel = builder
                    .comment("The maximum amount of possible undo operations that will be remembered at any time.")
                    .defineInRange("maxUndoLevel", 32, 1, Integer.MAX_VALUE);

            typeSlotsPerBag = builder
                    .comment("How many slots for unique blocks the bit bag have")
                    .defineInRange("typeSlotsPerBag", 12, 3, 12);

            typeSlotsPerBeaker = builder
                    .comment("How many slots for unique fluids the bit beaker has")
                    .defineInRange("typeSlotsPerBeaker", 4, 3, 12);

            bookmarksPerPalette = builder
                    .comment("How many slots you have for bookmarks in the palette")
                    .defineInRange("bookmarksPerPalette", 12, 3, 12);

            bitsPerTypeSlot = builder
                    .comment("How many bits fit in each slot of the bit bag")
                    .defineInRange("bitsPerTypeSlot", 131072, 1, 9223372036854775807L);

            chiselDurability = builder
                    .comment("How big should the durability of a chisel be, each bit modified takes one durability.")
                    .defineInRange("chiselDurability", 1048576, 1, Integer.MAX_VALUE); //Default is 16^5

            showBitsAvailableAsDurability = builder
                    .comment("Shows how many bits you've got left of the current type in the durability bar of the storage item.")
                    .define("showBitsAvailableAsDurability", true);

            builder.pop();

            SERVER = builder.build();
        }
    }
}
