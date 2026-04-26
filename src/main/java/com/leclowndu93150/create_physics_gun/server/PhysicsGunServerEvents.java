package com.leclowndu93150.create_physics_gun.server;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CreatePhysicsGun.MODID)
public final class PhysicsGunServerEvents {
    private PhysicsGunServerEvents() {}

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Post event) {
        PhysicsGunServerHandler.tickAll(event.getServer());
    }

    @SubscribeEvent
    public static void onLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof final ServerPlayer player) {
            PhysicsGunServerHandler.clear(player.getUUID());
        }
    }
}
