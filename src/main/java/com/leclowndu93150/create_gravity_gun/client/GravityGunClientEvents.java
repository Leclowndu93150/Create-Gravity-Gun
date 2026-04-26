package com.leclowndu93150.create_gravity_gun.client;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CreateGravityGun.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class GravityGunClientEvents {
    private GravityGunClientEvents() {}

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        GravityGunPartialModels.init();
    }
}
