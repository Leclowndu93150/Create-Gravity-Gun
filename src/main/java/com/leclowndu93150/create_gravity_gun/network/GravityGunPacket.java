package com.leclowndu93150.create_gravity_gun.network;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityGunPacket(Action action, double dx, double dy, double dz) implements CustomPacketPayload {
    public enum Action { TOGGLE_GRAB, PUNT, ADJUST_DISTANCE }

    public static final Type<GravityGunPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateGravityGun.MODID, "gun_action"));

    public static final StreamCodec<ByteBuf, GravityGunPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(i -> Action.values()[i], Enum::ordinal), GravityGunPacket::action,
            ByteBufCodecs.DOUBLE, GravityGunPacket::dx,
            ByteBufCodecs.DOUBLE, GravityGunPacket::dy,
            ByteBufCodecs.DOUBLE, GravityGunPacket::dz,
            GravityGunPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
