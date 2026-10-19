package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/** Allocates persistent, never-reused expedition IDs to prevent overwrites. */
public final class ExpeditionIndex extends SavedData {
    private int nextId = 1;

    public static ExpeditionIndex get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(ExpeditionIndex::new, ExpeditionIndex::load), "duskdelvers_index");
    }

    private static ExpeditionIndex load(CompoundTag nbt, HolderLookup.Provider registries) {
        ExpeditionIndex data = new ExpeditionIndex();
        data.nextId = Math.max(1, nbt.getInt("nextId"));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putInt("nextId", nextId);
        return nbt;
    }

    public int allocate() {
        int id = nextId++;
        setDirty();
        return id;
    }
}
