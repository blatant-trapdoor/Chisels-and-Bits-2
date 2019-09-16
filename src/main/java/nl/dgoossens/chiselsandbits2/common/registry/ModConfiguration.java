package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfiguration {
    public final ForgeConfigSpec SERVER;
    public final ForgeConfigSpec CLIENT;

    // --- CLIENT VALUES ---
    public ForgeConfigSpec.IntValue maxMillisecondsUploadingPerFrame;
    public ForgeConfigSpec.IntValue dynamicMaxConcurrentTessalators;
    public ForgeConfigSpec.IntValue dynamicModelFaceCount;

    public ForgeConfigSpec.DoubleValue radialMenuVolume;
    public ForgeConfigSpec.BooleanValue enableToolbarIcons;
    public ForgeConfigSpec.BooleanValue enableModeScrolling;

    public ForgeConfigSpec.BooleanValue enableVivecraftCompatibility;

    // --- SERVER VALUES ---
    public ForgeConfigSpec.DoubleValue maxDrawnRegionSize;

    public ForgeConfigSpec.IntValue typeSlotsPerBag;
    public ForgeConfigSpec.LongValue bitsPerTypeSlot;

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

            builder.pop();

            builder.comment("Performance Settings");
            builder.push("performance");

            maxMillisecondsUploadingPerFrame = builder
                    .comment("How many milliseconds the client can spend uploading models each frame")
                    .defineInRange("maxMillisecondsUploadingPerFrame", 15, 1, 1000);

            dynamicMaxConcurrentTessalators = builder
                    .comment("How many block models can fromName rendered at the same time. This will automatically be set to 2 if less than 1256 MB in memory is detected.")
                    .defineInRange("dynamicMaxConcurrentTessalators", 32, 1, 256);

            dynamicModelFaceCount = builder
                    .comment("The maximum amount of quads a dynamic model can contain")
                    .defineInRange("dynamicModelFaceCount", 40, 1, 256);

            builder.pop();

            builder.comment("Integration Settings");
            builder.push("integration");

            enableVivecraftCompatibility = builder
                    .comment("Turn on compatibility with Vivecraft, this turns the radial menu into an actual GUI instead of an overlay that can only be closed by clicking an option.")
                    .define("enableVivecraftCompatibility", false);

            builder.pop();

            CLIENT = builder.build();
        }

        //SERVER
        {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.comment("General Settings");
            builder.push("general");

            maxDrawnRegionSize = builder
                    .comment("At how many blocks the width/length of the drawn region selection should be capped")
                    .defineInRange("maxDrawnRegionSize", 4.0, 1.0, 16.0);

            typeSlotsPerBag = builder
                    .comment("How many slots for unique blocks does the bit bag have")
                    .defineInRange("typeSlotsPerBag", 12, 3, 12);

            bitsPerTypeSlot = builder
                    .comment("How many bits fit in each slot of the bit bag")
                    .defineInRange("bitsPerTypeSlot", 131072, 1, 9223372036854775807L);

            builder.pop();

            SERVER = builder.build();
        }
    }
}
