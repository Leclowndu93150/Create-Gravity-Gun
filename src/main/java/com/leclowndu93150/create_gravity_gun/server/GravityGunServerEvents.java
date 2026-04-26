package com.leclowndu93150.create_gravity_gun.server;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = CreateGravityGun.MODID)
public final class GravityGunServerEvents {
    private GravityGunServerEvents() {}

    @SubscribeEvent
    public static void onServerTick(final ServerTickEvent.Post event) {
        GravityGunServerHandler.tickAll(event.getServer());
    }

    @SubscribeEvent
    public static void onLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof final ServerPlayer player) {
            GravityGunServerHandler.clear(player.getUUID());
        }
    }
}
