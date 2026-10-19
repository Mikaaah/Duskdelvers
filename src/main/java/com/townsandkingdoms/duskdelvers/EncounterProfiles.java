package com.townsandkingdoms.duskdelvers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Resource datapacks can inject optional-mod mobs without binary dependencies.
 * data/<namespace>/expedition_encounters/*.json
 * Missing mod IDs and entity IDs are silently skipped during datapack reload.
 */
public final class EncounterProfiles extends SimpleJsonResourceReloadListener {
    private record Weighted(EntityType<?> entity, int minTier, int maxTier, int weight) {}
    private static volatile List<Weighted> pool = defaultPool();

    public EncounterProfiles() { super(new Gson(), "expedition_encounters"); }

    public static void registerReload(AddReloadListenerEvent event) {
        event.addListener(new EncounterProfiles());
    }

    private static List<Weighted> defaultPool() {
        return List.of(new Weighted(EntityType.ZOMBIE,1,5,40),
                new Weighted(EntityType.SKELETON,1,5,35),
                new Weighted(EntityType.SPIDER,1,5,20),
                new Weighted(EntityType.WITHER_SKELETON,3,5,10));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources,
                         ResourceManager manager, ProfilerFiller profiler) {
        List<Weighted> next = new ArrayList<>();
        for (Map.Entry<ResourceLocation,JsonElement> file:resources.entrySet()) {
            if (!file.getValue().isJsonObject()) continue;
            JsonObject content=file.getValue().getAsJsonObject();
            if (!content.has("entries") || !content.get("entries").isJsonArray()) continue;
            JsonArray entries=content.getAsJsonArray("entries");
            for (JsonElement entry:entries) {
                try {
                    JsonObject obj=entry.getAsJsonObject();
                    String required=obj.has("requires_mod") ? obj.get("requires_mod").getAsString() : "";
                    if (!required.isBlank() && (!DuskConfig.OPTIONAL_MOBS.get() ||
                            !DuskConfig.modAllowed(required))) continue;
                    ResourceLocation id=ResourceLocation.parse(obj.get("entity").getAsString());
                    if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) continue;
                    EntityType<?> entity=BuiltInRegistries.ENTITY_TYPE.get(id);
                    int min=Math.max(1,obj.has("min_tier") ? obj.get("min_tier").getAsInt() : 1);
                    int max=Math.min(5,obj.has("max_tier") ? obj.get("max_tier").getAsInt() : 5);
                    int weight=Math.max(0,obj.has("weight") ? obj.get("weight").getAsInt() : 10);
                    if (min<=max && weight>0) next.add(new Weighted(entity,min,max,weight));
                } catch (RuntimeException ignored) {
                    // Invalid entry: skip it instead of preventing a world from loading.
                }
            }
        }
        pool = next.isEmpty() ? defaultPool() : List.copyOf(next);
    }

    public static EntityType<?> pick(int tier,Random random) {
        List<Weighted> entries=pool;
        int total=0;
        for (Weighted entry: entries)
            if (tier>=entry.minTier && tier<=entry.maxTier) total+=entry.weight;
        if (total<=0) return EntityType.ZOMBIE;
        int pick=random.nextInt(total);
        for (Weighted entry: entries) {
            if (tier>=entry.minTier && tier<=entry.maxTier) {
                pick-=entry.weight;
                if (pick<0) return entry.entity;
            }
        }
        return EntityType.ZOMBIE;
    }

    public static int count() { return pool.size(); }
}
