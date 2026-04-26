package com.leclowndu93150.create_gravity_gun.network;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import com.leclowndu93150.create_gravity_gun.client.GravityGunClientVisuals;
import com.leclowndu93150.create_gravity_gun.client.GravityGunImposedMotion;
import com.leclowndu93150.create_gravity_gun.client.GravityGunSoundController;
import com.leclowndu93150.create_gravity_gun.server.GravityGunServerHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CreateGravityGun.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class GravityGunNetwork {
    private GravityGunNetwork() {}

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CreateGravityGun.MODID).versioned("1");
        registrar.playToServer(GravityGunPacket.TYPE, GravityGunPacket.STREAM_CODEC, GravityGunNetwork::handle);
        registrar.playToClient(GravityGunSyncPacket.TYPE, GravityGunSyncPacket.STREAM_CODEC, GravityGunNetwork::handleSync);
        registrar.playToClient(GravityGunMotionPacket.TYPE, GravityGunMotionPacket.STREAM_CODEC, GravityGunNetwork::handleMotion);
        registrar.playToClient(GravityGunFeedbackPacket.TYPE, GravityGunFeedbackPacket.STREAM_CODEC, GravityGunNetwork::handleFeedback);
    }

    private static void handleFeedback(final GravityGunFeedbackPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                GravityGunSoundController.onFeedback(packet);
            }
        });
    }

    private static void handleMotion(final GravityGunMotionPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                GravityGunImposedMotion.apply(packet);
            }
        });
    }

    private static void handle(final GravityGunPacket packet, final IPayloadContext context) {
        if (context.player() instanceof final ServerPlayer player) {
            context.enqueueWork(() -> GravityGunServerHandler.onPacket(packet, player));
        }
    }

    private static void handleSync(final GravityGunSyncPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                GravityGunClientVisuals.handleSync(packet);
            }
        });
    }
}
