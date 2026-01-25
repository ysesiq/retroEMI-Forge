package net.minecraft.util;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class JsonHelper {
	private static final Gson GSON = new GsonBuilder().create();

	public static boolean hasString(JsonObject object, String element) {
		return hasPrimitive(object, element) && object.getAsJsonPrimitive(element).isString();
	}

	public static boolean isString(JsonElement element) {
		return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
	}

	public static boolean hasNumber(JsonObject object, String element) {
		return hasPrimitive(object, element) && object.getAsJsonPrimitive(element).isNumber();
	}

	public static boolean hasBoolean(JsonObject object, String element) {
		return hasPrimitive(object, element) && object.getAsJsonPrimitive(element).isBoolean();
	}

	public static boolean isBoolean(JsonElement object) {
		return object.isJsonPrimitive() && object.getAsJsonPrimitive().isBoolean();
	}

	public static boolean hasArray(JsonObject object, String element) {
		return hasElement(object, element) && object.get(element).isJsonArray();
	}

	public static boolean hasJsonObject(JsonObject object, String element) {
		return hasElement(object, element) && object.get(element).isJsonObject();
	}

	public static boolean hasPrimitive(JsonObject object, String element) {
		return hasElement(object, element) && object.get(element).isJsonPrimitive();
	}

	public static boolean hasElement(JsonObject object, String element) {
		if (object == null) {
			return false;
		} else {
			return object.get(element) != null;
		}
	}

	public static String asString(JsonElement element, String name) {
		if (element.isJsonPrimitive()) {
			return element.getAsString();
		} else {
			throw new JsonSyntaxException("Expected " + name + " to be a string, was " + getType(element));
		}
	}

	public static String getString(JsonObject object, String element) {
		if (object.has(element)) {
			return asString(object.get(element), element);
		} else {
			throw new JsonSyntaxException("Missing " + element + ", expected to find a string");
		}
	}

	@Nullable
	public static String getString(JsonObject object, String element, @Nullable String defaultStr) {
		return object.has(element) ? asString(object.get(element), element) : defaultStr;
	}

	public static boolean asBoolean(JsonElement element, String name) {
		if (element.isJsonPrimitive()) {
			return element.getAsBoolean();
		} else {
			throw new JsonSyntaxException("Expected " + name + " to be a Boolean, was " + getType(element));
		}
	}

	public static boolean getBoolean(JsonObject object, String element) {
		if (object.has(element)) {
			return asBoolean(object.get(element), element);
		} else {
			throw new JsonSyntaxException("Missing " + element + ", expected to find a Boolean");
		}
	}

	public static boolean getBoolean(JsonObject object, String element, boolean defaultBoolean) {
		return object.has(element) ? asBoolean(object.get(element), element) : defaultBoolean;
	}

	public static double asDouble(JsonElement object, String name) {
		if (object.isJsonPrimitive() && object.getAsJsonPrimitive().isNumber()) {
			return object.getAsDouble();
		} else {
			throw new JsonSyntaxException("Expected " + name + " to be a Double, was " + getType(object));
		}
	}

	public static double getDouble(JsonObject object, String element) {
		if (object.has(element)) {
			return asDouble(object.get(element), element);
		} else {
			throw new JsonSyntaxException("Missing " + element + ", expected to find a Double");
		}
	}

	public static double getDouble(JsonObject object, String element, double defaultDouble) {
		return object.has(element) ? asDouble(object.get(element), element) : defaultDouble;
	}

	public static float asFloat(JsonElement element, String name) {
		if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
			return element.getAsFloat();
		} else {
			throw new JsonSyntaxException("Expected " + name + " to be a Float, was " + getType(element));
		}
	}

	public static float getFloat(JsonObject object, String element) {
		if (object.has(element)) {
			return asFloat(object.get(element), element);
		} else {
			throw new JsonSyntaxException("Missing " + element + ", expected to find a Float");
		}
	}

	public static float getFloat(JsonObject object, String element, float defaultFloat) {
		return object.has(element) ? asFloat(object.get(element), element) : defaultFloat;
	}

	public static long asLong(JsonElement element, String name) {
		if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
			return element.getAsLong();
		} else {
			throw new JsonSyntaxException("Expected " + name + " to be a Long, was " + getType(element));
		}
	}

	public static long getLong(JsonObject object, String name) {
		if (object.has(name)) {
			return asLong(object.get(name), name);
		} else {
			throw new JsonSyntaxException("Missing " + name + ", expected to find a Long");
		}
	}

	public static long getLong(JsonObject object, String element, long defaultLong) {
		return object.has(element) ? asLong(object.get(element), element) : defaultLong;
	}

	public static int asInt(JsonElement element, String name) {
		if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
			return element.getAsInt();
		} else {
			throw new JsonSyntaxException("Expected " + name + " to be a Int, was " + getType(element));
		}
	}

	public static int getInt(JsonObject object, String element) {
		if (object.has(element)) {
			return asInt(object.get(element), element);
		} else {
			throw new JsonSyntaxException("Missing " + element + ", expected to find a Int");
		}
	}

	public static int getInt(JsonObject object, String element, int defaultInt) {
		return object.has(element) ? asInt(object.get(element), element) : defaultInt;
	}

	public static JsonObject asObject(JsonElement element, String name) {
		if (element.isJsonObject()) {
			return element.getAsJsonObject();
		} else {
			throw new JsonSyntaxException("Expected " + name + " to be a JsonObject, was " + getType(element));
		}
	}

	public static JsonObject getObject(JsonObject object, String element) {
		if (object.has(element)) {
			return asObject(object.get(element), element);
		} else {
			throw new JsonSyntaxException("Missing " + element + ", expected to find a JsonObject");
		}
	}

	@Nullable
	public static JsonObject getObject(JsonObject object, String element, @Nullable JsonObject defaultObject) {
		return object.has(element) ? asObject(object.get(element), element) : defaultObject;
	}

	public static JsonArray asArray(JsonElement element, String name) {
		if (element.isJsonArray()) {
			return element.getAsJsonArray();
		} else {
			throw new JsonSyntaxException("Expected " + name + " to be a JsonArray, was " + getType(element));
		}
	}

	public static JsonArray getArray(JsonObject object, String element) {
		if (object.has(element)) {
			return asArray(object.get(element), element);
		} else {
			throw new JsonSyntaxException("Missing " + element + ", expected to find a JsonArray");
		}
	}

	@Nullable
	public static JsonArray getArray(JsonObject object, String name, @Nullable JsonArray defaultArray) {
		return object.has(name) ? asArray(object.get(name), name) : defaultArray;
	}

	public static <T> T deserialize(@Nullable JsonElement element, String name, JsonDeserializationContext context, Class<? extends T> type) {
		if (element != null) {
			return context.deserialize(element, type);
		} else {
			throw new JsonSyntaxException("Missing " + name);
		}
	}

	public static <T> T deserialize(JsonObject object, String element, JsonDeserializationContext context, Class<? extends T> type) {
		if (object.has(element)) {
			return deserialize(object.get(element), element, context, type);
		} else {
			throw new JsonSyntaxException("Missing " + element);
		}
	}

	@Nullable
	public static <T> T deserialize(JsonObject object, String element, @Nullable T defaultValue, JsonDeserializationContext context, Class<? extends T> type) {
		return (T)(object.has(element) ? deserialize(object.get(element), element, context, type) : defaultValue);
	}

	public static String getType(@Nullable JsonElement element) {
		String string = String.valueOf(element);
		if (element == null) {
			return "null (missing)";
		} else if (element.isJsonNull()) {
			return "null (json)";
		} else if (element.isJsonArray()) {
			return "an array (" + string + ")";
		} else if (element.isJsonObject()) {
			return "an object (" + string + ")";
		} else {
			if (element.isJsonPrimitive()) {
				JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
				if (jsonPrimitive.isNumber()) {
					return "a number (" + string + ")";
				}

				if (jsonPrimitive.isBoolean()) {
					return "a boolean (" + string + ")";
				}
			}

			return string;
		}
	}

	@Nullable
	public static <T> T m_vsbufiln(Gson gson, Reader reader, Class<T> class_, boolean bl) {
		try {
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(bl);
			return gson.<T>getAdapter(class_).read(jsonReader);
		} catch (IOException var5) {
			throw new JsonParseException(var5);
		}
	}

	public static <T> T deserialize(Gson gson, Reader reader, Class<T> type, boolean lenient) {
		T object = m_vsbufiln(gson, reader, type, lenient);
		if (object == null) {
			throw new JsonParseException("JSON data was null or empty");
		} else {
			return object;
		}
	}

	@Nullable
	public static <T> T m_boxypbwp(Gson gson, Reader reader, TypeToken<T> typeToken, boolean bl) {
		try {
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(bl);
			return gson.getAdapter(typeToken).read(jsonReader);
		} catch (IOException var5) {
			throw new JsonParseException(var5);
		}
	}

	public static <T> T deserialize(Gson gson, Reader reader, TypeToken<T> typeToken, boolean lenient) {
		T object = m_boxypbwp(gson, reader, typeToken, lenient);
		if (object == null) {
			throw new JsonParseException("JSON data was null or empty");
		} else {
			return object;
		}
	}

	@Nullable
	public static <T> T m_gludylcq(Gson gson, String string, TypeToken<T> typeToken, boolean bl) {
		return m_boxypbwp(gson, new StringReader(string), typeToken, bl);
	}

	public static <T> T deserialize(Gson gson, String content, Class<T> type, boolean lenient) {
		return deserialize(gson, new StringReader(content), type, lenient);
	}

	@Nullable
	public static <T> T m_haynlcxa(Gson gson, String string, Class<T> class_, boolean bl) {
		return m_vsbufiln(gson, new StringReader(string), class_, bl);
	}

	public static <T> T deserialize(Gson gson, Reader reader, TypeToken<T> typeToken) {
		return deserialize(gson, reader, typeToken, false);
	}

	@Nullable
	public static <T> T m_zmohitsy(Gson gson, String string, TypeToken<T> typeToken) {
		return m_gludylcq(gson, string, typeToken, false);
	}

	public static <T> T deserialize(Gson gson, Reader reader, Class<T> type) {
		return deserialize(gson, reader, type, false);
	}

	public static <T> T deserialize(Gson gson, String content, Class<T> type) {
		return deserialize(gson, content, type, false);
	}

	public static JsonObject deserialize(String content, boolean lenient) {
		return deserialize(new StringReader(content), lenient);
	}

	public static JsonObject deserialize(Reader reader, boolean lenient) {
		return deserialize(GSON, reader, JsonObject.class, lenient);
	}

	public static JsonObject deserialize(String content) {
		return deserialize(content, false);
	}

	public static JsonObject deserialize(Reader reader) {
		return deserialize(reader, false);
	}

	public static JsonArray deserializeArray(String content) {
		return deserializeArray(new StringReader(content));
	}

	public static JsonArray deserializeArray(Reader reader) {
		return deserialize(GSON, reader, JsonArray.class, false);
	}

	public static String toSortedString(JsonElement element) {
		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = new JsonWriter(stringWriter);

		try {
			writeValue(jsonWriter, element, Comparator.naturalOrder());
		} catch (IOException var4) {
			throw new AssertionError(var4);
		}

		return stringWriter.toString();
	}

	public static void writeValue(JsonWriter writer, @Nullable JsonElement element, @Nullable Comparator<String> comparator) throws IOException {
		if (element == null || element.isJsonNull()) {
			writer.nullValue();
		} else if (element.isJsonPrimitive()) {
			JsonPrimitive jsonPrimitive = element.getAsJsonPrimitive();
			if (jsonPrimitive.isNumber()) {
				writer.value(jsonPrimitive.getAsNumber());
			} else if (jsonPrimitive.isBoolean()) {
				writer.value(jsonPrimitive.getAsBoolean());
			} else {
				writer.value(jsonPrimitive.getAsString());
			}
		} else if (element.isJsonArray()) {
			writer.beginArray();

			for(JsonElement jsonElement : element.getAsJsonArray()) {
				writeValue(writer, jsonElement, comparator);
			}

			writer.endArray();
		} else {
			if (!element.isJsonObject()) {
				throw new IllegalArgumentException("Couldn't write " + element.getClass());
			}

			writer.beginObject();

			for(Entry<String, JsonElement> entry : sort(element.getAsJsonObject().entrySet(), comparator)) {
				writer.name(entry.getKey());
				writeValue(writer, entry.getValue(), comparator);
			}

			writer.endObject();
		}
	}

	private static Collection<Entry<String, JsonElement>> sort(Collection<Entry<String, JsonElement>> entries, @Nullable Comparator<String> comparator) {
		if (comparator == null) {
			return entries;
		} else {
			List<Entry<String, JsonElement>> list = new ArrayList(entries);
			list.sort(Entry.comparingByKey(comparator));
			return list;
		}
	}
}
