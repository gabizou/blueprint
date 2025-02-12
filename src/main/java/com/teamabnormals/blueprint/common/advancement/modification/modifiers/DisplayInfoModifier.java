package com.teamabnormals.blueprint.common.advancement.modification.modifiers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * An {@link IAdvancementModifier} implementation that modifies the {@link DisplayInfo} of an advancement.
 *
 * @author SmellyModder (Luke Tonon)
 */
public final class DisplayInfoModifier implements IAdvancementModifier<DisplayInfoModifier.Config> {
	private static final Field DISPLAY_INFO_FIELD = ObfuscationReflectionHelper.findField(Advancement.Builder.class, "f_138334_");
	private static final Field ICON_FIELD = ObfuscationReflectionHelper.findField(DisplayInfo.class, "f_14960_");
	private static final Field BACKGROUND_FIELD = ObfuscationReflectionHelper.findField(DisplayInfo.class, "f_14961_");
	private static final Field SHOW_TOAST_FIELD = ObfuscationReflectionHelper.findField(DisplayInfo.class, "f_14963_");

	private static ItemStack deserializeIcon(JsonObject object) {
		if (!object.has("item")) {
			throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
		} else {
			Item item = GsonHelper.getAsItem(object, "item");
			if (object.has("data")) {
				throw new JsonParseException("Disallowed data tag found");
			} else {
				ItemStack stack = new ItemStack(item);
				if (object.has("nbt")) {
					try {
						CompoundTag nbt = TagParser.parseTag(GsonHelper.convertToString(object.get("nbt"), "nbt"));
						stack.setTag(nbt);
					} catch (CommandSyntaxException syntax) {
						throw new JsonSyntaxException("Invalid nbt tag: " + syntax.getMessage());
					}
				}
				return stack;
			}
		}
	}

	@Override
	public void modify(Advancement.Builder builder, Config config) {
		if (config.mode == Mode.MODIFY) {
			try {
				DisplayInfo displayInfo = (DisplayInfo) DISPLAY_INFO_FIELD.get(builder);
				Component title = config.title.orElse(displayInfo.getTitle());
				Component description = config.description.orElse(displayInfo.getDescription());
				ItemStack icon = config.icon.orElse((ItemStack) ICON_FIELD.get(displayInfo));
				ResourceLocation background = config.background.orElse((ResourceLocation) BACKGROUND_FIELD.get(displayInfo));
				FrameType frameType = config.frame.orElse(displayInfo.getFrame());
				boolean showToast = config.showToast.orElse(SHOW_TOAST_FIELD.getBoolean(displayInfo));
				boolean announceToChat = config.announceToChat.orElse(displayInfo.shouldAnnounceChat());
				boolean hidden = config.hidden.orElse(displayInfo.isHidden());
				builder.display(new DisplayInfo(icon, title, description, background, frameType, showToast, announceToChat, hidden));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} else {
			builder.display(new DisplayInfo(config.icon.orElse(ItemStack.EMPTY), config.title.orElse(TextComponent.EMPTY), config.description.orElse(TextComponent.EMPTY), config.background.orElse(null), config.frame.orElse(FrameType.TASK), config.showToast.orElse(true), config.announceToChat.orElse(true), config.hidden.orElse(false)));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public JsonElement serialize(Config config, Void additional) throws JsonParseException {
		JsonObject jsonObject = new JsonObject();
		config.mode.serialize(jsonObject);
		config.title.ifPresent(title -> jsonObject.add("title", Component.Serializer.toJsonTree(title)));
		config.description.ifPresent(description -> jsonObject.add("description", Component.Serializer.toJsonTree(description)));
		config.icon.ifPresent(icon -> {
			JsonObject iconObject = new JsonObject();
			iconObject.addProperty("item", Registry.ITEM.getKey(icon.getItem()).toString());
			if (icon.hasTag()) {
				iconObject.addProperty("nbt", icon.getTag().toString());
			}
			jsonObject.add("icon", iconObject);
		});
		config.background.ifPresent(background -> jsonObject.addProperty("background", background.toString()));
		config.frame.ifPresent(frame -> jsonObject.addProperty("frame", frame.getName()));
		config.showToast.ifPresent(showToast -> jsonObject.addProperty("show_toast", showToast));
		config.announceToChat.ifPresent(announceToChat -> jsonObject.addProperty("announce_to_chat", announceToChat));
		config.hidden.ifPresent(hidden -> jsonObject.addProperty("hidden", hidden));
		return jsonObject;
	}

	@Override
	public Config deserialize(JsonElement element, DeserializationContext additional) throws JsonParseException {
		JsonObject object = element.getAsJsonObject();
		Mode mode = Mode.deserialize(object);
		Optional<Component> title = GsonHelper.isValidNode(object, "title") ? Optional.ofNullable(Component.Serializer.fromJson(object.get("title"))) : Optional.empty();
		Optional<Component> description = GsonHelper.isValidNode(object, "description") ? Optional.ofNullable(Component.Serializer.fromJson(object.get("description"))) : Optional.empty();
		Optional<ItemStack> icon = GsonHelper.isValidNode(object, "icon") ? Optional.of(deserializeIcon(GsonHelper.getAsJsonObject(object, "icon"))) : Optional.empty();
		Optional<ResourceLocation> background = GsonHelper.isValidNode(object, "background") ? Optional.of(new ResourceLocation(GsonHelper.getAsString(object, "background"))) : Optional.empty();
		Optional<FrameType> frameType = GsonHelper.isValidNode(object, "frame") ? Optional.of(FrameType.byName(GsonHelper.getAsString(object, "frame"))) : Optional.empty();
		Optional<Boolean> showToast = GsonHelper.isValidNode(object, "show_toast") ? Optional.of(GsonHelper.getAsBoolean(object, "show_toast")) : Optional.empty();
		Optional<Boolean> announceToChat = GsonHelper.isValidNode(object, "announce_to_chat") ? Optional.of(GsonHelper.getAsBoolean(object, "announce_to_chat")) : Optional.empty();
		Optional<Boolean> hidden = GsonHelper.isValidNode(object, "hidden") ? Optional.of(GsonHelper.getAsBoolean(object, "hidden")) : Optional.empty();
		return new Config(mode, title, description, icon, background, frameType, showToast, announceToChat, hidden);
	}

	public static class Config {
		private final Mode mode;
		private final Optional<Component> title;
		private final Optional<Component> description;
		private final Optional<ItemStack> icon;
		private final Optional<ResourceLocation> background;
		private final Optional<FrameType> frame;
		private final Optional<Boolean> showToast;
		private final Optional<Boolean> announceToChat;
		private final Optional<Boolean> hidden;

		public Config(Mode mode, Optional<Component> title, Optional<Component> description, Optional<ItemStack> icon, Optional<ResourceLocation> background, Optional<FrameType> frame, Optional<Boolean> showToast, Optional<Boolean> announceToChat, Optional<Boolean> hidden) {
			this.mode = mode;
			this.title = title;
			this.description = description;
			this.icon = icon;
			this.background = background;
			this.frame = frame;
			this.showToast = showToast;
			this.announceToChat = announceToChat;
			this.hidden = hidden;
		}
	}
}
