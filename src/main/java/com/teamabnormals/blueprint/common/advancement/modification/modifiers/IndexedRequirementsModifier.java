package com.teamabnormals.blueprint.common.advancement.modification.modifiers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Map;
import java.util.Optional;

/**
 * An {@link IAdvancementModifier} implementation that modifies a specific requirement array of an advancement's requirements.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class IndexedRequirementsModifier implements IAdvancementModifier<IndexedRequirementsModifier.Config> {

	@Override
	public void modify(Advancement.Builder builder, Config config) {
		Map<String, Criterion> criteria = builder.getCriteria();
		try {
			String[][] requirementsArray = (String[][]) CriteriaModifier.REQUIREMENTS_FIELD.get(builder);
			int index = config.index;
			if (config.mode == Mode.MODIFY) {
				config.requirements.ifPresent(strings -> requirementsArray[index] = ArrayUtils.addAll(requirementsArray[index], strings));
			} else {
				criteria.clear();
				requirementsArray[index] = config.requirements.orElse(new String[]{});
			}
			config.criterionMap.ifPresent(criteria::putAll);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public JsonElement serialize(Config config, Void additional) throws JsonParseException {
		JsonObject jsonObject = new JsonObject();
		config.mode.serialize(jsonObject);
		jsonObject.addProperty("index", config.index);
		config.criterionMap.ifPresent(map -> {
			JsonObject criteria = new JsonObject();
			map.forEach((key, criterion) -> criteria.add(key, criterion.serializeToJson()));
			jsonObject.add("criteria", criteria);
		});
		config.requirements.ifPresent(requirements -> {
			JsonArray jsonarray = new JsonArray();
			for (String s : requirements) {
				jsonarray.add(s);
			}
			jsonObject.add("requirements", jsonarray);
		});
		return jsonObject;
	}

	@Override
	public Config deserialize(JsonElement element, DeserializationContext additional) throws JsonParseException {
		JsonObject object = element.getAsJsonObject();
		Mode mode = Mode.deserialize(object);
		int index = GsonHelper.getAsInt(object, "index");
		Optional<Map<String, Criterion>> criterionMap = GsonHelper.isValidNode(object, "criteria") ? Optional.of(Criterion.criteriaFromJson(object.getAsJsonObject("criteria"), additional)) : Optional.empty();
		Optional<String[]> requirements = Optional.empty();
		if (criterionMap.isPresent()) {
			Map<String, Criterion> map = criterionMap.get();
			if (map.isEmpty()) {
				throw new JsonParseException("Criteria cannot be empty! Don't include it instead");
			}
			if (GsonHelper.isValidNode(object, "requirements")) {
				JsonArray requirementsArray = GsonHelper.getAsJsonArray(object, "requirements");
				String[] strings = new String[requirementsArray.size()];
				if (strings.length == 0) {
					throw new JsonParseException("Requirements cannot be empty!");
				}
				for (int i = 0; i < strings.length; i++) {
					String string = requirementsArray.get(i).getAsString();
					if (!map.containsKey(string)) {
						throw new JsonParseException("Unknown required criterion '" + string + "'");
					}
					strings[i] = string;
				}
				for (String key : map.keySet()) {
					if (!ArrayUtils.contains(strings, key)) {
						throw new JsonParseException("Criterion '" + key + "' isn't a requirement for completion. This isn't supported behaviour, all criteria must be required.");
					}
				}
				requirements = Optional.of(strings);
			}
		}
		return new Config(mode, index, criterionMap, requirements);
	}

	public static class Config {
		private final Mode mode;
		private final int index;
		private final Optional<Map<String, Criterion>> criterionMap;
		private final Optional<String[]> requirements;

		public Config(Mode mode, int index, Optional<Map<String, Criterion>> criterionMap, Optional<String[]> requirements) {
			this.index = index;
			this.mode = mode;
			this.criterionMap = criterionMap;
			this.requirements = requirements;
		}
	}

}
