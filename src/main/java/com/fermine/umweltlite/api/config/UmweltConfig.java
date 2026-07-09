package com.fermine.umweltlite.api.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class UmweltConfig {
    public static final ModConfigSpec SPEC;

    // --- Config Fields ---
    public static final ModConfigSpec.BooleanValue ENABLE_HOST_HIJACKING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("UmweltLite Cognitive & Mechanical Core Settings").push("general");

        ENABLE_HOST_HIJACKING = builder
                .comment("If true, vanilla/other mobs will be replaced by their Umwelt cognitive equivalents upon spawning.",
                        "Default is false to prevent unsolicited host hijacking.")
                .define("enableHostHijacking", false);

        builder.pop();
        SPEC = builder.build();
    }
}