package com.leclowndu93150.create_gravity_gun.network;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

public record GravityGunSyncPacket(TargetType targetType, int entityId, Optional<UUID> subLevelId,
                                   double x, double y, double z) implements CustomPacketPayload {
    public enum TargetType { NONE, ENTITY, SUB_LEVEL }

    public static final Type<GravityGunSyncPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateGravityGun.MODID, "gun_sync"));

    public static final StreamCodec<ByteBuf, GravityGunSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public GravityGunSyncPacket decode(final ByteBuf buf) {
            return new GravityGunSyncPacket(
                    TargetType.values()[ByteBufCodecs.VAR_INT.decode(buf)],
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf),
                    ByteBufCodecs.DOUBLE.decode(buf));
        }

        @Override
        public void encode(final ByteBuf buf, final GravityGunSyncPacket packet) {
            ByteBufCodecs.VAR_INT.encode(buf, packet.targetType().ordinal());
            ByteBufCodecs.VAR_INT.encode(buf, packet.entityId());
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC).encode(buf, packet.subLevelId());
            ByteBufCodecs.DOUBLE.encode(buf, packet.x());
            ByteBufCodecs.DOUBLE.encode(buf, packet.y());
            ByteBufCodecs.DOUBLE.encode(buf, packet.z());
        }
    };

    public static GravityGunSyncPacket none() {
        return new GravityGunSyncPacket(TargetType.NONE, -1, Optional.empty(), 0.0, 0.0, 0.0);
    }

    public static GravityGunSyncPacket entity(final int entityId) {
        return new GravityGunSyncPacket(TargetType.ENTITY, entityId, Optional.empty(), 0.0, 0.0, 0.0);
    }

    public static GravityGunSyncPacket subLevel(final UUID subLevelId, final double x, final double y, final double z) {
        return new GravityGunSyncPacket(TargetType.SUB_LEVEL, -1, Optional.of(subLevelId), x, y, z);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
