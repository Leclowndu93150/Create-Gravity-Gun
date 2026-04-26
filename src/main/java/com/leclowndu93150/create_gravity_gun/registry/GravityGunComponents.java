package com.leclowndu93150.create_gravity_gun.registry;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GravityGunComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateGravityGun.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SOUNDS_ENABLED =
            COMPONENTS.registerComponentType("sounds_enabled", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    private GravityGunComponents() {}

    public static void register(final IEventBus modBus) {
        COMPONENTS.register(modBus);
    }
}
