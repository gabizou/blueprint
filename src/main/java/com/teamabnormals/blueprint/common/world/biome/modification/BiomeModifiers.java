package com.teamabnormals.blueprint.common.world.biome.modification;

import com.google.gson.JsonElement;
import com.teamabnormals.blueprint.common.world.biome.modification.modifiers.*;
import com.teamabnormals.blueprint.core.util.modification.ModifierRegistry;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraftforge.event.world.BiomeLoadingEvent;

/**
 * The registry class for {@link com.teamabnormals.blueprint.common.world.biome.modification.modifiers.IBiomeModifier} implementations.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class BiomeModifiers {
	public static final ModifierRegistry<BiomeLoadingEvent, RegistryWriteOps<JsonElement>, RegistryReadOps<JsonElement>> REGISTRY = new ModifierRegistry<>();

	public static final BiomeCarversModifier CARVERS = REGISTRY.register("carvers", new BiomeCarversModifier());
	public static final BiomeCategoryModifier CATEGORY = REGISTRY.register("category", new BiomeCategoryModifier());
	public static final BiomeClimateSettingsModifier CLIMATE_SETTINGS = REGISTRY.register("climate_settings", new BiomeClimateSettingsModifier());
	public static final BiomeFeaturesModifier FEATURES = REGISTRY.register("features", new BiomeFeaturesModifier());
	public static final BiomeSpawnsModifier SPAWNS = REGISTRY.register("spawns", new BiomeSpawnsModifier());
	public static final BiomeSpecialEffectsModifier SPECIAL_EFFECTS = REGISTRY.register("special_effects", new BiomeSpecialEffectsModifier());
}
