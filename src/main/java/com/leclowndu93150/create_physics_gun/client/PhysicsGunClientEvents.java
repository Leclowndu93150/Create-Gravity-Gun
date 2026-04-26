package com.leclowndu93150.create_physics_gun.client;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CreatePhysicsGun.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PhysicsGunClientEvents {
    private PhysicsGunClientEvents() {}

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        PhysicsGunPartialModels.init();
    }
}
