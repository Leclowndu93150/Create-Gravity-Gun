package com.leclowndu93150.create_physics_gun.network;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PhysicsGunMotionPacket(boolean active, double vx, double vy, double vz) implements CustomPacketPayload {
    public static final Type<PhysicsGunMotionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreatePhysicsGun.MODID, "gun_motion"));

    public static final StreamCodec<ByteBuf, PhysicsGunMotionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PhysicsGunMotionPacket::active,
            ByteBufCodecs.DOUBLE, PhysicsGunMotionPacket::vx,
            ByteBufCodecs.DOUBLE, PhysicsGunMotionPacket::vy,
            ByteBufCodecs.DOUBLE, PhysicsGunMotionPacket::vz,
            PhysicsGunMotionPacket::new);

    public static PhysicsGunMotionPacket clear() {
        return new PhysicsGunMotionPacket(false, 0.0, 0.0, 0.0);
    }

    public static PhysicsGunMotionPacket of(final double vx, final double vy, final double vz) {
        return new PhysicsGunMotionPacket(true, vx, vy, vz);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
