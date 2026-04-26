package com.leclowndu93150.create_gravity_gun.item;

import com.leclowndu93150.create_gravity_gun.registry.GravityGunComponents;
import dev.simulated_team.simulated.content.physics_staff.PhysicsStaffItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class GravityGunItem extends PhysicsStaffItem {
    public static final int ORANGE_NAME_COLOR = 0xff8a3a;

    public GravityGunItem(final Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(final ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack)).withColor(ORANGE_NAME_COLOR);
    }

    @Override
    public void appendHoverText(final ItemStack stack, final Item.TooltipContext context, final List<Component> tooltip, final TooltipFlag flag) {
        tooltip.add(Component.translatable("item.create_gravity_gun.gravity_gun.tooltip.grab").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.create_gravity_gun.gravity_gun.tooltip.punt").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.create_gravity_gun.gravity_gun.tooltip.scroll").withStyle(ChatFormatting.GRAY));
        final boolean soundsOn = stack.getOrDefault(GravityGunComponents.SOUNDS_ENABLED.get(), Boolean.TRUE);
        tooltip.add(Component.translatable("item.create_gravity_gun.gravity_gun.tooltip.toggle_sounds",
                Component.translatable(soundsOn
                        ? "item.create_gravity_gun.gravity_gun.tooltip.sounds_on"
                        : "item.create_gravity_gun.gravity_gun.tooltip.sounds_off")
                        .withStyle(soundsOn ? ChatFormatting.GREEN : ChatFormatting.RED))
                .withStyle(ChatFormatting.GRAY));
    }
}
