package dev.emi.emi.stack.serializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.TagEmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import shim.net.minecraft.registry.tag.TagKey;
import shim.net.minecraft.util.JsonHelper;
import net.minecraft.util.ResourceLocation;

public class TagEmiIngredientSerializer implements EmiIngredientSerializer<TagEmiIngredient> {
	static final Pattern STACK_REGEX = Pattern.compile("^#([\\w_\\-.:]+):([\\w_\\-.]+):([\\w_\\-./]+)(\\{.*\\})?$");

	@Override
	public String getType() {
		return "tag";
	}

	@Override
	public EmiIngredient deserialize(JsonElement element) {
		if (JsonHelper.isString(element)) {
			String s = element.getAsString();
			Matcher m = STACK_REGEX.matcher(s);
			if (m.matches()) {
				TagKey.Type registry = TagKey.Type.valueOf(m.group(1));
				ResourceLocation id = EmiPort.id(m.group(2), m.group(3));
				return EmiIngredient.of(new TagKey<>(id, registry), 1);
			}
		} else if (element.isJsonObject()) {
			JsonObject json = element.getAsJsonObject();
			TagKey.Type registry = TagKey.Type.valueOf(json.get("registry").getAsString());
			ResourceLocation id = EmiPort.id(json.get("id").getAsString());
			long amount = JsonHelper.getLong(json, "amount", 1);
			float chance = JsonHelper.getFloat(json, "chance", 1);
			EmiIngredient stack = EmiIngredient.of(new TagKey<>(id, registry), amount);
			if (chance != 1) {
				stack.setChance(chance);
			}
			return stack;
		}
		return EmiStack.EMPTY;
	}

	@Override
	public JsonElement serialize(TagEmiIngredient stack) {
		if (stack.getAmount() == 1 && stack.getChance() == 1) {
			String type = switch(stack.key.getType().toString()) {
				case "minecraft:item" -> "item";
				case "minecraft:fluid" -> "fluid";
				default -> null;
			};
			return new JsonPrimitive("#" + type + ":" + stack.key.id());
		} else {
			JsonObject json = new JsonObject();
			json.addProperty("type", "tag");
			json.addProperty("registry", stack.key.getType().toString());
			json.addProperty("id", stack.key.id().getResourcePath());
			if (stack.getAmount() != 1) {
				json.addProperty("amount", stack.getAmount());
			}
			if (stack.getChance() != 1) {
				json.addProperty("chance", stack.getChance());
			}
			return json;
		}
	}
}
