package com.leclowndu93150.create_gravity_gun.registry;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GravityGunSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, CreateGravityGun.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> PICKUP = register("pickup");
    public static final DeferredHolder<SoundEvent, SoundEvent> DROP = register("drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRYFIRE = register("dryfire");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOO_HEAVY = register("too_heavy");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLAWS_OPEN = register("claws_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> CLAWS_CLOSE = register("claws_close");
    public static final DeferredHolder<SoundEvent, SoundEvent> HOLD_LOOP = register("hold_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> PHYSGUN_OFF = register("physgun_off");
    public static final DeferredHolder<SoundEvent, SoundEvent> LAUNCH = register("launch");

    private GravityGunSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(final String name) {
        final ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateGravityGun.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(final IEventBus modBus) {
        SOUNDS.register(modBus);
    }
}
