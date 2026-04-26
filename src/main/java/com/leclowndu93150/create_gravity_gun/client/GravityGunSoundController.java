package com.leclowndu93150.create_gravity_gun.client;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import com.leclowndu93150.create_gravity_gun.item.GravityGunItem;
import com.leclowndu93150.create_gravity_gun.network.GravityGunFeedbackPacket;
import com.leclowndu93150.create_gravity_gun.registry.GravityGunComponents;
import com.leclowndu93150.create_gravity_gun.registry.GravityGunSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = CreateGravityGun.MODID, value = Dist.CLIENT)
public final class GravityGunSoundController {
    private static final float FEEDBACK_VOLUME = 0.5f;
    private static final float HOLD_LOOP_MAX_VOLUME = 0.4f;

    private static HoldLoop activeLoop;

    private GravityGunSoundController() {}

    public static void onFeedback(final GravityGunFeedbackPacket packet) {
        if (packet.kind() == GravityGunFeedbackPacket.Kind.LAUNCH) {
            GravityGunViewKick.punch(-2.5f, (Minecraft.getInstance().level != null
                    ? (Minecraft.getInstance().level.random.nextFloat() - 0.5f) : 0.0f) * 1.6f);
        }
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !soundsEnabled(player)) return;
        final SoundEvent sound = switch (packet.kind()) {
            case PICKUP -> GravityGunSounds.PICKUP.get();
            case DROP -> GravityGunSounds.DROP.get();
            case LAUNCH -> GravityGunSounds.LAUNCH.get();
            case DRYFIRE -> GravityGunSounds.DRYFIRE.get();
            case TOO_HEAVY -> GravityGunSounds.TOO_HEAVY.get();
        };
        player.playSound(sound, FEEDBACK_VOLUME, 1.0f);
    }

    @SubscribeEvent
    public static void onPlayerTick(final PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        final Minecraft mc = Minecraft.getInstance();
        if (mc.player != player) return;
        tickHoldLoop(mc, player, GravityGunClientVisuals.isGrabbing() && soundsEnabled(player));
    }

    private static void tickHoldLoop(final Minecraft mc, final LocalPlayer player, final boolean shouldPlay) {
        if (shouldPlay) {
            if (activeLoop == null || activeLoop.isStopped()) {
                activeLoop = new HoldLoop(player);
                mc.getSoundManager().play(activeLoop);
            }
        } else if (activeLoop != null) {
            activeLoop.fadeOut();
            activeLoop = null;
        }
    }

    static boolean soundsEnabled(final LocalPlayer player) {
        final ItemStack stack = gunStack(player);
        if (stack == null) return false;
        return stack.getOrDefault(GravityGunComponents.SOUNDS_ENABLED.get(), Boolean.TRUE);
    }

    static ItemStack gunStack(final LocalPlayer player) {
        if (player.getMainHandItem().getItem() instanceof GravityGunItem) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof GravityGunItem) return player.getOffhandItem();
        return null;
    }

    static boolean isHoldingGun(final LocalPlayer player) {
        return gunStack(player) != null;
    }

    private static final class HoldLoop extends AbstractTickableSoundInstance {
        private final LocalPlayer player;
        private boolean fading;

        private HoldLoop(final LocalPlayer player) {
            super(GravityGunSounds.HOLD_LOOP.get(), SoundSource.PLAYERS, RandomSource.create());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.0f;
            this.pitch = 0.6f;
            this.relative = true;
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }

        void fadeOut() {
            fading = true;
        }

        @Override
        public void tick() {
            if (player.isRemoved() || !isHoldingGun(player) || !soundsEnabled(player)) {
                stop();
                return;
            }
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
            if (fading) {
                this.volume = Math.max(0.0f, this.volume - 0.15f);
                this.pitch = Math.max(0.5f, this.pitch - 0.05f);
                if (this.volume <= 0.0f) stop();
            } else {
                this.volume = Math.min(HOLD_LOOP_MAX_VOLUME, this.volume + 0.06f);
                this.pitch = Math.min(1.0f, this.pitch + 0.05f);
            }
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }
    }
}
