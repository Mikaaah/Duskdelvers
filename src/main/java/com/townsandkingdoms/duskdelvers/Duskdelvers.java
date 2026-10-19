package com.townsandkingdoms.duskdelvers;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The dependency-free foundation of Duskdelvers. All expedition content is
 * deliberately isolated from optional Towns & Kingdoms integrations.
 */
@Mod(Duskdelvers.MOD_ID)
public final class Duskdelvers {
    public static final String MOD_ID = "duskdelvers";

    public Duskdelvers(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(DuskCommands::register);
    }
}
