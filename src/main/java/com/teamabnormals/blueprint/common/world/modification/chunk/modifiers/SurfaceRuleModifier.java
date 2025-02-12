package com.teamabnormals.blueprint.common.world.modification.chunk.modifiers;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamabnormals.blueprint.core.Blueprint;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.function.Supplier;

/**
 * An {@link UnsafeChunkGeneratorModifier} subclass that modifies the surface rule of a {@link ChunkGenerator} instance.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class SurfaceRuleModifier extends UnsafeChunkGeneratorModifier<SurfaceRuleModifier.Config> {
	public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				SurfaceRules.RuleSource.CODEC.fieldOf("surface_rule").forGetter(modifier -> modifier.surfaceRule),
				Codec.BOOL.optionalFieldOf("replace", false).forGetter(modifier -> modifier.replace)
		).apply(instance, Config::new);
	});
	private static final Field NOISE_GENERATOR_SETTINGS = ObfuscationReflectionHelper.findField(NoiseBasedChunkGenerator.class, "f_64318_");

	@SuppressWarnings({"unchecked", "deprecation"})
	@Override
	public void modify(ChunkGenerator chunkGenerator, Config config) {
		if (chunkGenerator instanceof NoiseBasedChunkGenerator) {
			long fieldOffset = UNSAFE.objectFieldOffset(NOISE_GENERATOR_SETTINGS);
			SurfaceRules.RuleSource newRuleSource;
			NoiseGeneratorSettings settings = ((Supplier<NoiseGeneratorSettings>) UNSAFE.getObject(chunkGenerator, fieldOffset)).get();
			if (config.replace) newRuleSource = config.surfaceRule;
			else {
				SurfaceRules.RuleSource ruleSource = settings.surfaceRule();
				if (ruleSource instanceof SurfaceRules.SequenceRuleSource sequenceRuleSource) {
					//Surface rules are processed per block so optimizing sequence rule performance by not wrapping is optimal here
					var sequence = sequenceRuleSource.sequence();
					ArrayList<SurfaceRules.RuleSource> newSequence = new ArrayList<>(sequence.size() + 1);
					newSequence.add(config.surfaceRule);
					newSequence.addAll(sequence);
					newRuleSource = SurfaceRules.sequence(newSequence.toArray(new SurfaceRules.RuleSource[0]));
				} else newRuleSource = SurfaceRules.sequence(config.surfaceRule, ruleSource);
			}
			NoiseGeneratorSettings newSettings = new NoiseGeneratorSettings(settings.structureSettings(), settings.noiseSettings(), settings.getDefaultBlock(), settings.getDefaultFluid(), newRuleSource, settings.seaLevel(), settings.disableMobGeneration(), settings.isAquifersEnabled(), settings.isNoiseCavesEnabled(), settings.isOreVeinsEnabled(), settings.isNoodleCavesEnabled(), settings.useLegacyRandomSource());
			UNSAFE.putObject(chunkGenerator, fieldOffset, (Supplier<NoiseGeneratorSettings>) () -> newSettings);
		} else
			Blueprint.LOGGER.warn("Could not apply surface rule modifier because " + chunkGenerator + " was not an instance of NoiseBasedChunkGenerator");
	}

	@Override
	public JsonElement serialize(Config config, RegistryWriteOps<JsonElement> additional) throws JsonParseException {
		var dataResult = CODEC.encodeStart(additional, config);
		var result = dataResult.result();
		if (result.isPresent()) return result.get();
		throw new JsonParseException(dataResult.error().get().message());
	}

	@Override
	public Config deserialize(JsonElement element, RegistryReadOps<JsonElement> additional) throws JsonParseException {
		var dataResult = CODEC.decode(additional, element);
		var result = dataResult.result();
		if (result.isPresent()) return result.get().getFirst();
		throw new JsonParseException(dataResult.error().get().message());
	}

	/**
	 * The config class for a configured {@link SurfaceRuleModifier} instance.
	 *
	 * @author SmellyModder (Luke Tonon)
	 */
	public static record Config(SurfaceRules.RuleSource surfaceRule, boolean replace) {}
}
