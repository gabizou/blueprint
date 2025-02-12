package com.teamabnormals.blueprint.common.loot.modification.modifiers;

import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import com.teamabnormals.blueprint.core.util.DataUtil;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link ILootModifier} that modifies the entries of a {@link LootPool} in a {@link net.minecraft.world.level.storage.loot.LootTable}.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class LootPoolEntriesModifier implements ILootModifier<LootPoolEntriesModifier.Config> {
	public static final Field ENTRIES = ObfuscationReflectionHelper.findField(LootPool.class, "f_79023_");

	@SuppressWarnings("unchecked")
	@Override
	public void modify(LootTableLoadEvent object, Config config) {
		try {
			LootPool pool = ((List<LootPool>) LootPoolsModifier.POOLS.get(object.getTable())).get(config.index);
			LootPoolEntryContainer[] lootEntries = (LootPoolEntryContainer[]) ENTRIES.get(pool);
			if (config.replace) {
				lootEntries = config.entries.toArray(LootPoolEntryContainer[]::new);
			} else {
				lootEntries = DataUtil.concatArrays(lootEntries, config.entries.toArray(LootPoolEntryContainer[]::new));
			}
			ENTRIES.set(pool, lootEntries);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public JsonElement serialize(Config config, Gson gson) throws JsonParseException {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("replace", config.replace);
		jsonObject.addProperty("index", config.index);
		JsonArray entries = new JsonArray();
		for (LootPoolEntryContainer lootEntry : config.entries) {
			entries.add(gson.toJsonTree(lootEntry));
		}
		jsonObject.add("entries", entries);
		return jsonObject;
	}

	@Override
	public Config deserialize(JsonElement element, Pair<Gson, PredicateManager> additional) throws JsonParseException {
		JsonObject jsonObject = element.getAsJsonObject();
		int index = GsonHelper.getAsInt(jsonObject, "index");
		if (index < 0) {
			throw new JsonParseException("'index' must be 0 or greater!");
		}
		List<LootPoolEntryContainer> entries = new ArrayList<>();
		JsonArray entriesArray = jsonObject.getAsJsonArray("entries");
		Gson gson = additional.getFirst();
		entriesArray.forEach(entry -> entries.add(gson.fromJson(entry, LootPoolEntryContainer.class)));
		return new Config(GsonHelper.getAsBoolean(jsonObject, "replace"), index, entries);
	}

	public static class Config {
		private final boolean replace;
		private final int index;
		private final List<LootPoolEntryContainer> entries;

		public Config(boolean replace, int index, List<LootPoolEntryContainer> entries) {
			this.replace = replace;
			this.index = index;
			this.entries = entries;
		}
	}
}
