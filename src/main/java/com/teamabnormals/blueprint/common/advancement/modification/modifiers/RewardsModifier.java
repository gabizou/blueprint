package com.teamabnormals.blueprint.common.advancement.modification.modifiers;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * An {@link IAdvancementModifier} implementation that modifies the rewards of an advancement.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class RewardsModifier implements IAdvancementModifier<RewardsModifier.Config> {

	private static JsonArray serializeResourceList(List<ResourceLocation> resources) {
		JsonArray array = new JsonArray();
		resources.forEach(resourceLocation -> array.add(resourceLocation.toString()));
		return array;
	}

	private static Optional<List<ResourceLocation>> deserializeResourceList(JsonObject object, String key) {
		if (GsonHelper.isValidNode(object, key)) {
			List<ResourceLocation> resourceLocations = Lists.newArrayList();
			object.getAsJsonArray(key).forEach(element -> resourceLocations.add(new ResourceLocation(element.getAsString())));
			return Optional.of(resourceLocations);
		}
		return Optional.empty();
	}

	@Override
	public void modify(Advancement.Builder builder, Config config) {
		if (config.mode == Mode.MODIFY) {
			AdvancementRewards rewards = builder.rewards;
			AdvancementRewards.Builder rewardsBuilder = new AdvancementRewards.Builder();
			rewardsBuilder.addExperience(rewards.experience);
			rewardsBuilder.loot.addAll(Arrays.asList(rewards.loot));
			for (ResourceLocation recipe : rewards.recipes) {
				rewardsBuilder.addRecipe(recipe);
			}
			rewardsBuilder.function = rewards.function.getId();

			config.experience.ifPresent(rewardsBuilder::addExperience);
			config.loot.ifPresent(rewardsBuilder.loot::addAll);
			config.recipes.ifPresent(rewardsBuilder.recipes::addAll);
			config.function.ifPresent(function -> rewardsBuilder.function = function);
			builder.rewards(rewardsBuilder);
		} else {
			AdvancementRewards.Builder rewardsBuilder = new AdvancementRewards.Builder();
			config.experience.ifPresent(rewardsBuilder::addExperience);
			config.loot.ifPresent(loot -> {
				rewardsBuilder.loot.clear();
				rewardsBuilder.loot.addAll(loot);
			});
			config.recipes.ifPresent(recipes -> {
				rewardsBuilder.recipes.clear();
				rewardsBuilder.recipes.addAll(recipes);
			});
			config.function.ifPresent(function -> rewardsBuilder.function = function);
			builder.rewards(rewardsBuilder);
		}
	}

	@Override
	public JsonElement serialize(Config config, Void additional) throws JsonParseException {
		JsonObject object = new JsonObject();
		config.mode.serialize(object);
		config.experience.ifPresent(experience -> object.addProperty("experience", experience));
		config.loot.ifPresent(loot -> object.add("loot", serializeResourceList(loot)));
		config.recipes.ifPresent(recipes -> object.add("recipes", serializeResourceList(recipes)));
		config.function.ifPresent(function -> object.addProperty("function", function.toString()));
		return object;
	}

	@Override
	public Config deserialize(JsonElement element, DeserializationContext additional) throws JsonParseException {
		JsonObject object = element.getAsJsonObject();
		Mode mode = Mode.deserialize(object);
		Optional<Integer> experience = GsonHelper.isValidNode(object, "experience") ? Optional.of(GsonHelper.getAsInt(object, "experience")) : Optional.empty();
		Optional<List<ResourceLocation>> loot = deserializeResourceList(object, "loot");
		Optional<List<ResourceLocation>> recipes = deserializeResourceList(object, "recipes");
		Optional<ResourceLocation> function = GsonHelper.isValidNode(object, "function") ? Optional.of(new ResourceLocation(GsonHelper.getAsString(object, "function"))) : Optional.empty();
		return new Config(mode, experience, loot, recipes, function);
	}

	public static class Config {
		private final Mode mode;
		private final Optional<Integer> experience;
		private final Optional<List<ResourceLocation>> loot;
		private final Optional<List<ResourceLocation>> recipes;
		private final Optional<ResourceLocation> function;

		public Config(Mode mode, Optional<Integer> experience, Optional<List<ResourceLocation>> loot, Optional<List<ResourceLocation>> recipes, Optional<ResourceLocation> function) {
			this.mode = mode;
			this.experience = experience;
			this.loot = loot;
			this.recipes = recipes;
			this.function = function;
		}
	}

}
