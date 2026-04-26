package com.leclowndu93150.create_gravity_gun.client;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import com.leclowndu93150.create_gravity_gun.item.GravityGunItem;
import com.leclowndu93150.create_gravity_gun.network.GravityGunPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CreateGravityGun.MODID, value = Dist.CLIENT)
public final class GravityGunClientInput {
    private GravityGunClientInput() {}

    private static boolean holdingGun() {
        final LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return false;
        final ItemStack main = p.getMainHandItem();
        final ItemStack off = p.getOffhandItem();
        return main.getItem() instanceof GravityGunItem || off.getItem() instanceof GravityGunItem;
    }

    @SubscribeEvent
    public static void onMouseButton(final InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().screen != null) return;
        if (!holdingGun()) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            send(new GravityGunPacket(GravityGunPacket.Action.TOGGLE_GRAB, 0, 0, 0));
            if (GravityGunClientVisuals.isGrabbing()) {
                GravityGunClientVisuals.clear();
            }
            event.setCanceled(true);
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            send(new GravityGunPacket(GravityGunPacket.Action.PUNT, 0, 0, 0));
            GravityGunClientVisuals.clear();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScroll(final InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().screen != null) return;
        if (!holdingGun() || !GravityGunClientVisuals.isGrabbing()) return;
        send(new GravityGunPacket(GravityGunPacket.Action.ADJUST_DISTANCE, 0, event.getScrollDeltaY() * 0.5, 0));
        event.setCanceled(true);
    }

    private static void send(final GravityGunPacket packet) {
        PacketDistributor.sendToServer(packet);
    }
}
