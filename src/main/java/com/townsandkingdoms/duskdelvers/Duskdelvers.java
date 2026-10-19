package com.townsandkingdoms.duskdelvers;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Duskdelvers.MOD_ID)
public final class Duskdelvers {
    public static final String MOD_ID = "duskdelvers";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredItem<Item> DELVERS_COMPASS = ITEMS.register(
            "delvers_compass", () -> new DelversCompass(new Item.Properties().stacksTo(1)));

    public Duskdelvers(IEventBus bus, ModContainer container) {
        ITEMS.register(bus);
        bus.addListener(this::creativeTabs);
        container.registerConfig(ModConfig.Type.SERVER, DuskConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(DuskCommands::register);
        NeoForge.EVENT_BUS.addListener(ExpeditionRuntime::tick);
        NeoForge.EVENT_BUS.addListener(ExpeditionRuntime::clonePlayer);
        NeoForge.EVENT_BUS.addListener(EncounterProfiles::registerReload);
    }

    private void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DELVERS_COMPASS.get());
        }
    }
}
