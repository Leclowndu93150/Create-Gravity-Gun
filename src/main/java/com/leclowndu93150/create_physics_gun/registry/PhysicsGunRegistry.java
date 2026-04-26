package com.leclowndu93150.create_physics_gun.registry;

import com.leclowndu93150.create_physics_gun.CreatePhysicsGun;
import com.leclowndu93150.create_physics_gun.item.PhysicsGunItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PhysicsGunRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreatePhysicsGun.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreatePhysicsGun.MODID);

    public static final DeferredItem<PhysicsGunItem> PHYSICS_GUN =
            ITEMS.register("physics_gun",
                    () -> new PhysicsGunItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_physics_gun"))
                    .icon(() -> PHYSICS_GUN.get().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(PHYSICS_GUN.get()))
                    .build());

    private PhysicsGunRegistry() {}

    public static void register(final IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(PhysicsGunRegistry::onBuildTab);
    }

    private static void onBuildTab(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "tools_and_utilities"))) {
            event.accept(PHYSICS_GUN);
        }
    }
}
