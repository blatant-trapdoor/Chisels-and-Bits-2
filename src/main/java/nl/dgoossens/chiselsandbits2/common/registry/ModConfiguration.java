package nl.dgoossens.chiselsandbits2.common.registry;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfiguration {
    public final ForgeConfigSpec SERVER;
    public final ForgeConfigSpec CLIENT;

    // --- CLIENT VALUES ---
    public ForgeConfigSpec.IntValue maxMillisecondsUploadingPerFrame;
    public ForgeConfigSpec.IntValue dynamicMaxConcurrentTessalators;

    // --- SERVER VALUES ---
    public ForgeConfigSpec.DoubleValue maxDrawnRegionSize;

    /**
     * Initialise all configuration values.
     */
    public ModConfiguration() {
        //CLIENT
        {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.comment("Performance Settings");
            builder.push("performance");

            maxMillisecondsUploadingPerFrame = builder
                    .comment("How many milliseconds the client can spend uploading models each frame")
                    .defineInRange("maxMillisecondsUploadingPerFrame", 15, 1, 1000);

            dynamicMaxConcurrentTessalators = builder
                    .comment("How many block models can get rendered at the same time. This will automatically be set to 2 if less than 1256 MB in memory is detected.")
                    .defineInRange("dynamicMaxConcurrentTessalators", 32, 1, 256);

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

            builder.pop();

            SERVER = builder.build();
        }
    }
}
