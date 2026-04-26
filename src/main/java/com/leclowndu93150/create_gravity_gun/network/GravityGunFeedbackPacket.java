package com.leclowndu93150.create_gravity_gun.network;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record GravityGunFeedbackPacket(Kind kind) implements CustomPacketPayload {
    public enum Kind { PICKUP, DROP, LAUNCH, DRYFIRE, TOO_HEAVY }

    public static final Type<GravityGunFeedbackPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateGravityGun.MODID, "gun_feedback"));

    public static final StreamCodec<ByteBuf, GravityGunFeedbackPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, packet -> packet.kind().ordinal(),
            ordinal -> new GravityGunFeedbackPacket(Kind.values()[ordinal]));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
