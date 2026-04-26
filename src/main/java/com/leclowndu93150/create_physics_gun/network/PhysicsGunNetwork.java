package com.leclowndu93150.create_physics_gun.network;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import com.leclowndu93150.create_physics_gun.client.PhysicsGunClientVisuals;
import com.leclowndu93150.create_physics_gun.client.PhysicsGunImposedMotion;
import com.leclowndu93150.create_physics_gun.server.PhysicsGunServerHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CreatePhysicsGun.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class PhysicsGunNetwork {
    private PhysicsGunNetwork() {}

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CreatePhysicsGun.MODID).versioned("1");
        registrar.playToServer(PhysicsGunPacket.TYPE, PhysicsGunPacket.STREAM_CODEC, PhysicsGunNetwork::handle);
        registrar.playToClient(PhysicsGunSyncPacket.TYPE, PhysicsGunSyncPacket.STREAM_CODEC, PhysicsGunNetwork::handleSync);
        registrar.playToClient(PhysicsGunMotionPacket.TYPE, PhysicsGunMotionPacket.STREAM_CODEC, PhysicsGunNetwork::handleMotion);
    }

    private static void handleMotion(final PhysicsGunMotionPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                PhysicsGunImposedMotion.apply(packet);
            }
        });
    }

    private static void handle(final PhysicsGunPacket packet, final IPayloadContext context) {
        if (context.player() instanceof final ServerPlayer player) {
            context.enqueueWork(() -> PhysicsGunServerHandler.onPacket(packet, player));
        }
    }

    private static void handleSync(final PhysicsGunSyncPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                PhysicsGunClientVisuals.handleSync(packet);
            }
        });
    }
}
