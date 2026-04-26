package com.leclowndu93150.create_gravity_gun.config;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = CreateGravityGun.MODID)
public final class GravityGunConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue GRAB_RANGE;
    public static final ModConfigSpec.DoubleValue MIN_HOLD_DISTANCE;
    public static final ModConfigSpec.DoubleValue MAX_HOLD_DISTANCE;
    public static final ModConfigSpec.DoubleValue CARRY_MAX_SPEED;
    public static final ModConfigSpec.DoubleValue PUNT_VELOCITY;
    public static final ModConfigSpec.DoubleValue MAX_PICKUP_MASS;
    public static final ModConfigSpec.IntValue DENY_SOUND_COOLDOWN_TICKS;
    public static final ModConfigSpec.DoubleValue SCROLL_SENSITIVITY;

    public static volatile double grabRange = 24.0;
    public static volatile double minHoldDistance = 1.5;
    public static volatile double maxHoldDistance = 12.0;
    public static volatile double carryMaxSpeed = 25.4;
    public static volatile double puntVelocity = 38.1;
    public static volatile double maxPickupMass = 250.0;
    public static volatile int denySoundCooldownTicks = 20;
    public static volatile double scrollSensitivity = 0.5;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Gravity Gun behavior tuning. Defaults match Half-Life 2 Source SDK 2013.").push("gravity_gun");

        GRAB_RANGE = builder
                .comment("Maximum distance at which the gun can pick up a target, in blocks. HL2 default: 24.")
                .defineInRange("grab_range", 24.0, 1.0, 128.0);

        MIN_HOLD_DISTANCE = builder
                .comment("Minimum carry distance when scrolling toward you, in blocks. HL2 default: 1.5.")
                .defineInRange("min_hold_distance", 1.5, 0.5, 32.0);

        MAX_HOLD_DISTANCE = builder
                .comment("Maximum carry distance when scrolling away, in blocks. HL2 default: 12.")
                .defineInRange("max_hold_distance", 12.0, 1.0, 64.0);

        CARRY_MAX_SPEED = builder
                .comment("Maximum carry velocity in blocks per second while reeling a target to the hold point. HL2 default: 25.4.")
                .defineInRange("carry_max_speed", 25.4, 0.1, 200.0);

        PUNT_VELOCITY = builder
                .comment("Impulse velocity applied on punt (left click), in blocks per second. HL2 default: 38.1.")
                .defineInRange("punt_velocity", 38.1, 0.1, 400.0);

        MAX_PICKUP_MASS = builder
                .comment("Maximum sub-level mass that the gun can lift. HL2 default: 250 (physcannon_maxmass).")
                .defineInRange("max_pickup_mass", 250.0, 0.0, 1.0e9);

        DENY_SOUND_COOLDOWN_TICKS = builder
                .comment("Minimum ticks between successive too-heavy / failed-pickup sounds.")
                .defineInRange("deny_sound_cooldown_ticks", 20, 0, 200);

        SCROLL_SENSITIVITY = builder
                .comment("Hold-distance change per scroll step, in blocks.")
                .defineInRange("scroll_sensitivity", 0.5, 0.05, 5.0);

        builder.pop();
        SPEC = builder.build();
    }

    private GravityGunConfig() {}

    public static void register(final ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) refresh();
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) refresh();
    }

    private static void refresh() {
        grabRange = GRAB_RANGE.get();
        minHoldDistance = MIN_HOLD_DISTANCE.get();
        maxHoldDistance = MAX_HOLD_DISTANCE.get();
        carryMaxSpeed = CARRY_MAX_SPEED.get();
        puntVelocity = PUNT_VELOCITY.get();
        maxPickupMass = MAX_PICKUP_MASS.get();
        denySoundCooldownTicks = DENY_SOUND_COOLDOWN_TICKS.get();
        scrollSensitivity = SCROLL_SENSITIVITY.get();
    }
}
