package com.leclowndu93150.create_physics_gun.network;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PhysicsGunPacket(Action action, double dx, double dy, double dz) implements CustomPacketPayload {
    public enum Action { TOGGLE_GRAB, PUNT, ADJUST_DISTANCE }

    public static final Type<PhysicsGunPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreatePhysicsGun.MODID, "gun_action"));

    public static final StreamCodec<ByteBuf, PhysicsGunPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(i -> Action.values()[i], Enum::ordinal), PhysicsGunPacket::action,
            ByteBufCodecs.DOUBLE, PhysicsGunPacket::dx,
            ByteBufCodecs.DOUBLE, PhysicsGunPacket::dy,
            ByteBufCodecs.DOUBLE, PhysicsGunPacket::dz,
            PhysicsGunPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
