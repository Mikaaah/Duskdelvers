package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
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
    public static final Logger LOG = LoggerFactory.getLogger("Duskdelvers");
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE,MOD_ID);
    public static final Supplier<Feature<NoneFeatureConfiguration>> CAVERN_CACHE =
            FEATURES.register("cavern_cache", () -> new CavernCacheFeature(NoneFeatureConfiguration.CODEC));
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredBlock<GatewayBlock> RETURN_GATEWAY = makeGateway("return_gateway", 0);
    private static final DeferredBlock<GatewayBlock> TIER_1_GATEWAY = makeGateway("tier_1_portal", 1);
    private static final DeferredBlock<GatewayBlock> TIER_2_GATEWAY = makeGateway("tier_2_portal", 2);
    private static final DeferredBlock<GatewayBlock> TIER_3_GATEWAY = makeGateway("tier_3_portal", 3);
    private static final DeferredBlock<GatewayBlock> TIER_4_GATEWAY = makeGateway("tier_4_portal", 4);
    private static final DeferredBlock<GatewayBlock> TIER_5_GATEWAY = makeGateway("tier_5_portal", 5);

    private static DeferredBlock<GatewayBlock> makeGateway(String name, int tier) {
        return BLOCKS.register(name, () -> new GatewayBlock(tier,
                BlockBehaviour.Properties.of().noCollission().noOcclusion()
                        .strength(-1.0F, 3600000.0F).lightLevel(state -> 11)));
    }

    public static GatewayBlock gatewayForTier(int tier) {
        return switch (tier) {
            case 0 -> RETURN_GATEWAY.get();
            case 1 -> TIER_1_GATEWAY.get();
            case 2 -> TIER_2_GATEWAY.get();
            case 3 -> TIER_3_GATEWAY.get();
            case 4 -> TIER_4_GATEWAY.get();
            case 5 -> TIER_5_GATEWAY.get();
            default -> throw new IllegalArgumentException("Unsupported gateway tier: " + tier);
        };
    }
    public static final DeferredItem<Item> DELVERS_COMPASS = ITEMS.register(
            "delvers_compass", () -> new DelversCompass(new Item.Properties().stacksTo(1)));

    public Duskdelvers(IEventBus bus, ModContainer container) {
        ITEMS.register(bus);
        FEATURES.register(bus);
        BLOCKS.register(bus);
        bus.addListener(this::creativeTabs);
        container.registerConfig(ModConfig.Type.SERVER, DuskConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(DuskCommands::register);
        NeoForge.EVENT_BUS.addListener(ExpeditionRuntime::tick);
        NeoForge.EVENT_BUS.addListener(ExpeditionRuntime::clonePlayer);
        NeoForge.EVENT_BUS.addListener(ExpeditionRuntime::onCacheOpened);
        NeoForge.EVENT_BUS.addListener(CavernsDeathProtection::onDeath);
        NeoForge.EVENT_BUS.addListener(CavernsDeathProtection::onDrops);
        NeoForge.EVENT_BUS.addListener(CavernsDeathProtection::onXp);
        NeoForge.EVENT_BUS.addListener(CavernsDeathProtection::onClone);
        NeoForge.EVENT_BUS.addListener(CavernsDeathProtection::onLogout);
        NeoForge.EVENT_BUS.addListener(EncounterProfiles::registerReload);
    }

    private void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DELVERS_COMPASS.get());
        }
    }
}
