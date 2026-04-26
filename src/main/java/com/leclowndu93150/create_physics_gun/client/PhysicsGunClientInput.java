package com.leclowndu93150.create_physics_gun.client;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import com.leclowndu93150.create_physics_gun.item.PhysicsGunItem;
import com.leclowndu93150.create_physics_gun.network.PhysicsGunPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = CreatePhysicsGun.MODID, value = Dist.CLIENT)
public final class PhysicsGunClientInput {
    private PhysicsGunClientInput() {}

    private static boolean holdingGun() {
        final LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return false;
        final ItemStack main = p.getMainHandItem();
        final ItemStack off = p.getOffhandItem();
        return main.getItem() instanceof PhysicsGunItem || off.getItem() instanceof PhysicsGunItem;
    }

    @SubscribeEvent
    public static void onMouseButton(final InputEvent.MouseButton.Pre event) {
        if (Minecraft.getInstance().screen != null) return;
        if (!holdingGun()) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            send(new PhysicsGunPacket(PhysicsGunPacket.Action.TOGGLE_GRAB, 0, 0, 0));
            if (PhysicsGunClientVisuals.isGrabbing()) {
                PhysicsGunClientVisuals.clear();
            }
            event.setCanceled(true);
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            send(new PhysicsGunPacket(PhysicsGunPacket.Action.PUNT, 0, 0, 0));
            PhysicsGunClientVisuals.clear();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScroll(final InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().screen != null) return;
        if (!holdingGun() || !PhysicsGunClientVisuals.isGrabbing()) return;
        send(new PhysicsGunPacket(PhysicsGunPacket.Action.ADJUST_DISTANCE, 0, event.getScrollDeltaY() * 0.5, 0));
        event.setCanceled(true);
    }

    private static void send(final PhysicsGunPacket packet) {
        PacketDistributor.sendToServer(packet);
    }
}
