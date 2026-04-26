package com.leclowndu93150.create_gravity_gun.registry;

import com.leclowndu93150.create_gravity_gun.CreateGravityGun;
import com.leclowndu93150.create_gravity_gun.item.GravityGunItem;
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

public final class GravityGunRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateGravityGun.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateGravityGun.MODID);

    public static final DeferredItem<GravityGunItem> PHYSICS_GUN =
            ITEMS.register("gravity_gun",
                    () -> new GravityGunItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.create_gravity_gun"))
                    .icon(() -> PHYSICS_GUN.get().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(PHYSICS_GUN.get()))
                    .build());

    private GravityGunRegistry() {}

    public static void register(final IEventBus modBus) {
        ITEMS.register(modBus);
        TABS.register(modBus);
        modBus.addListener(GravityGunRegistry::onBuildTab);
    }

    private static void onBuildTab(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "tools_and_utilities"))) {
            event.accept(PHYSICS_GUN);
        }
    }
}
