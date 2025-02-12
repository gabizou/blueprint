package com.teamabnormals.blueprint.common.advancement.modification.modifiers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * An {@link IAdvancementModifier} implementation that modifies effects_changed type criteria.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class EffectsChangedModifier implements IAdvancementModifier<EffectsChangedModifier.Config> {
	private static final Field INSTANCE_EFFECTS_FIELD = ObfuscationReflectionHelper.findField(EffectsChangedTrigger.TriggerInstance.class, "f_26774_");
	private static final Field PREDICATE_EFFECTS_FIELD = ObfuscationReflectionHelper.findField(MobEffectsPredicate.class, "f_56548_");

	@SuppressWarnings("unchecked")
	@Override
	public void modify(Advancement.Builder builder, Config config) {
		String criteriaKey = config.criteria;
		Criterion criterion = builder.getCriteria().get(criteriaKey);
		if (criterion != null) {
			CriterionTriggerInstance instance = criterion.getTrigger();
			if (instance instanceof EffectsChangedTrigger.TriggerInstance) {
				try {
					Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> effectMap = (Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate>) PREDICATE_EFFECTS_FIELD.get(INSTANCE_EFFECTS_FIELD.get(instance));
					Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate> configEffectMap = (Map<MobEffect, MobEffectsPredicate.MobEffectInstancePredicate>) PREDICATE_EFFECTS_FIELD.get(config.mobEffectsPredicate);
					if (config.removes) {
						configEffectMap.forEach((effect, instancePredicate) -> effectMap.remove(effect));
					} else {
						effectMap.putAll(configEffectMap);
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		} else {
			throw new IllegalArgumentException("Unknown criteria: " + criteriaKey);
		}
	}

	@Override
	public JsonElement serialize(Config config, Void additional) throws JsonParseException {
		JsonObject object = new JsonObject();
		object.addProperty("criteria", config.criteria);
		object.addProperty("removes", config.removes);
		object.add("effects", config.mobEffectsPredicate.serializeToJson());
		return object;
	}

	@Override
	public Config deserialize(JsonElement element, DeserializationContext additional) throws JsonParseException {
		JsonObject object = element.getAsJsonObject();
		String criteria = GsonHelper.getAsString(object, "criteria");
		boolean removes = GsonHelper.getAsBoolean(object, "removes");
		MobEffectsPredicate effectsPredicate = MobEffectsPredicate.fromJson(object.get("effects"));
		return new Config(criteria, removes, effectsPredicate);
	}

	public static class Config {
		private final String criteria;
		private final boolean removes;
		private final MobEffectsPredicate mobEffectsPredicate;

		public Config(String criteria, boolean removes, MobEffectsPredicate mobEffectsPredicate) {
			this.criteria = criteria;
			this.removes = removes;
			this.mobEffectsPredicate = mobEffectsPredicate;
		}
	}
}
