package com.leclowndu93150.create_gravity_gun.network;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityGunMotionPacket(boolean active, double vx, double vy, double vz) implements CustomPacketPayload {
    public static final Type<GravityGunMotionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateGravityGun.MODID, "gun_motion"));

    public static final StreamCodec<ByteBuf, GravityGunMotionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, GravityGunMotionPacket::active,
            ByteBufCodecs.DOUBLE, GravityGunMotionPacket::vx,
            ByteBufCodecs.DOUBLE, GravityGunMotionPacket::vy,
            ByteBufCodecs.DOUBLE, GravityGunMotionPacket::vz,
            GravityGunMotionPacket::new);

    public static GravityGunMotionPacket clear() {
        return new GravityGunMotionPacket(false, 0.0, 0.0, 0.0);
    }

    public static GravityGunMotionPacket of(final double vx, final double vy, final double vz) {
        return new GravityGunMotionPacket(true, vx, vy, vz);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
