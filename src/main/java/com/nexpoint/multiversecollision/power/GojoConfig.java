package com.nexpoint.multiversecollision.power;

import net.minecraftforge.common.ForgeConfigSpec;

/** Valores provisórios de balanceamento. Alteráveis em serverconfig sem recompilar. */
public final class GojoConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.DoubleValue REGEN, INFINITY_DRAIN, BLUE_DRAIN;
    public static final ForgeConfigSpec.IntValue BLUE_COST, RED_COST, REVERSE_COST, PURPLE_COST, AMPLIFIED_COST, DOMAIN_COST;
    public static final ForgeConfigSpec.IntValue BLUE_RADIUS, RED_RANGE, PURPLE_RANGE, AMPLIFIED_RADIUS, DOMAIN_RADIUS;
    public static final ForgeConfigSpec.IntValue BLUE_DURATION, DOMAIN_DURATION, SIX_EYES_RANGE, BLOCKS_PER_TICK;
    public static final ForgeConfigSpec.IntValue BLUE_COOLDOWN, RED_COOLDOWN, REVERSE_COOLDOWN, PURPLE_COOLDOWN, AMPLIFIED_COOLDOWN, DOMAIN_COOLDOWN;
    public static final ForgeConfigSpec.BooleanValue TERRAIN, PVP;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("gojo");
        REGEN = b.defineInRange("regenPerSecond", 2.0, 0, 100);
        INFINITY_DRAIN = b.defineInRange("infinityPerSecond", 0.25, 0, 100);
        BLUE_DRAIN = b.defineInRange("bluePerSecond", 3.0, 0, 100);
        BLUE_COST = b.defineInRange("blueStartCost", 5, 0, 100);
        RED_COST = b.defineInRange("redCost", 20, 0, 100);
        REVERSE_COST = b.defineInRange("reverseCost", 25, 0, 100);
        PURPLE_COST = b.defineInRange("purpleCost", 55, 0, 100);
        AMPLIFIED_COST = b.defineInRange("amplifiedCost", 90, 0, 100);
        DOMAIN_COST = b.defineInRange("domainCost", 80, 0, 100);
        BLUE_RADIUS = b.defineInRange("blueRadius", 6, 1, 16);
        RED_RANGE = b.defineInRange("redRange", 24, 2, 64);
        PURPLE_RANGE = b.defineInRange("purpleRange", 36, 2, 96);
        AMPLIFIED_RADIUS = b.defineInRange("amplifiedRadius", 12, 1, 24);
        DOMAIN_RADIUS = b.defineInRange("domainRadius", 12, 1, 32);
        BLUE_DURATION = b.defineInRange("blueReleaseTicks", 40, 1, 200);
        DOMAIN_DURATION = b.defineInRange("domainTicks", 160, 1, 1200);
        SIX_EYES_RANGE = b.defineInRange("sixEyesRange", 16, 1, 64);
        BLOCKS_PER_TICK = b.defineInRange("blocksPerTick", 4, 0, 64);
        BLUE_COOLDOWN = b.defineInRange("blueCooldownTicks", 100, 0, 12000);
        RED_COOLDOWN = b.defineInRange("redCooldownTicks", 80, 0, 12000);
        REVERSE_COOLDOWN = b.defineInRange("reverseCooldownTicks", 100, 0, 12000);
        PURPLE_COOLDOWN = b.defineInRange("purpleCooldownTicks", 300, 0, 12000);
        AMPLIFIED_COOLDOWN = b.defineInRange("amplifiedCooldownTicks", 600, 0, 12000);
        DOMAIN_COOLDOWN = b.defineInRange("domainCooldownTicks", 1200, 0, 12000);
        TERRAIN = b.define("affectTerrain", true);
        PVP = b.define("allowPvp", false);
        b.pop();
        SPEC = b.build();
    }

    private GojoConfig() {}
}
