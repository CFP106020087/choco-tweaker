package com.chocotweak.geckolib.animation;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads Bedrock animation JSON into GeoAnimation objects.
 * Backport from YSM GeckoLib 1.20.1.
 * 
 * Animation JSON format (Bedrock 1.8.0):
 * {
 * "format_version": "1.8.0",
 * "animations": {
 * "animation_name": {
 * "loop": true/false,
 * "animation_length": 1.0,
 * "bones": {
 * "BoneName": {
 * "rotation": { "0.0": [x, y, z], "0.5": [x, y, z] },
 * "position": { ... },
 * "scale": { ... }
 * }
 * }
 * }
 * }
 * }
 */
public class GeoAnimationLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, Map<String, GeoAnimation>> ANIMATION_CACHE = new HashMap<>();

    /**
     * Load all animations from a resource location.
     */
    public static Map<String, GeoAnimation> load(ResourceLocation location) {
        if (ANIMATION_CACHE.containsKey(location)) {
            return ANIMATION_CACHE.get(location);
        }

        try {
            InputStream stream = Minecraft.getMinecraft().getResourceManager()
                    .getResource(location).getInputStream();
            Map<String, GeoAnimation> animations = load(stream);
            if (animations != null) {
                ANIMATION_CACHE.put(location, animations);
            }
            return animations;
        } catch (Exception e) {
            LOGGER.error("Failed to load animations from {}: {}", location, e.getMessage());
            return new HashMap<>();
        }
    }

    public static Map<String, GeoAnimation> load(InputStream stream) {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            return parseAnimations(root);
        } catch (Exception e) {
            LOGGER.error("Failed to parse animations: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private static Map<String, GeoAnimation> parseAnimations(JsonObject root) {
        Map<String, GeoAnimation> result = new HashMap<>();

        if (!root.has("animations")) {
            return result;
        }

        JsonObject animationsObj = root.getAsJsonObject("animations");
        for (Map.Entry<String, JsonElement> entry : animationsObj.entrySet()) {
            String animName = entry.getKey();
            JsonObject animJson = entry.getValue().getAsJsonObject();

            GeoAnimation animation = parseAnimation(animName, animJson);
            if (animation != null) {
                result.put(animName, animation);
            }
        }

        return result;
    }

    private static GeoAnimation parseAnimation(String name, JsonObject json) {
        boolean loop = json.has("loop") && json.get("loop").getAsBoolean();
        float length = json.has("animation_length") ? json.get("animation_length").getAsFloat() : 1.0f;

        GeoAnimation animation = new GeoAnimation(name, length, loop);

        if (json.has("bones")) {
            JsonObject bonesObj = json.getAsJsonObject("bones");
            for (Map.Entry<String, JsonElement> entry : bonesObj.entrySet()) {
                String boneName = entry.getKey();
                JsonObject boneJson = entry.getValue().getAsJsonObject();

                GeoBoneAnimation boneAnim = parseBoneAnimation(boneName, boneJson);
                animation.boneAnimations.put(boneName, boneAnim);
            }
        }

        return animation;
    }

    private static GeoBoneAnimation parseBoneAnimation(String boneName, JsonObject json) {
        GeoBoneAnimation boneAnim = new GeoBoneAnimation(boneName);

        // Parse rotation keyframes
        if (json.has("rotation")) {
            parseChannel(json.get("rotation"), boneAnim.rotationKeyframes);
        }

        // Parse position keyframes
        if (json.has("position")) {
            parseChannel(json.get("position"), boneAnim.positionKeyframes);
        }

        // Parse scale keyframes
        if (json.has("scale")) {
            parseChannel(json.get("scale"), boneAnim.scaleKeyframes);
        }

        return boneAnim;
    }

    private static void parseChannel(JsonElement channelElement, java.util.List<GeoKeyframe> keyframes) {
        if (channelElement.isJsonObject()) {
            // Object format: { "0.0": [x,y,z], "0.5": {...} }
            JsonObject channelObj = channelElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : channelObj.entrySet()) {
                float time = Float.parseFloat(entry.getKey());
                float[] values = parseKeyframeValue(entry.getValue());
                // Skip keyframes that contain Molang expressions (NaN values)
                if (containsMolang(values)) {
                    continue;
                }
                String lerp = parseLerpType(entry.getValue());
                keyframes.add(new GeoKeyframe(time, values, lerp));
            }
            // Sort by time
            keyframes.sort((a, b) -> Float.compare(a.time, b.time));
        } else if (channelElement.isJsonArray()) {
            // Array format: [x, y, z] - static value at time 0
            float[] values = parseVec3(channelElement.getAsJsonArray());
            if (!containsMolang(values)) {
                keyframes.add(new GeoKeyframe(0, values, "linear"));
            }
        }
    }

    private static boolean containsMolang(float[] values) {
        for (float v : values) {
            if (Float.isNaN(v))
                return true;
        }
        return false;
    }

    private static float[] parseKeyframeValue(JsonElement element) {
        if (element.isJsonArray()) {
            return parseVec3(element.getAsJsonArray());
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("vector")) {
                return parseVec3(obj.getAsJsonArray("vector"));
            }
            // Default for complex keyframe objects
            return new float[] { 0, 0, 0 };
        } else if (element.isJsonPrimitive()) {
            // Single value
            float val = parseSingleValue(element);
            return new float[] { val, val, val };
        }
        return new float[] { 0, 0, 0 };
    }

    private static String parseLerpType(JsonElement element) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("lerp_mode")) {
                return obj.get("lerp_mode").getAsString();
            }
        }
        return "linear";
    }

    private static float[] parseVec3(JsonArray array) {
        float[] result = new float[3];
        for (int i = 0; i < Math.min(3, array.size()); i++) {
            result[i] = parseSingleValue(array.get(i));
        }
        return result;
    }

    /**
     * Parse a single numeric value, ignoring Molang expressions.
     */
    private static float parseSingleValue(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isNumber()) {
                return prim.getAsFloat();
            } else if (prim.isString()) {
                // Try to parse as number, default to 0 for Molang
                try {
                    return Float.parseFloat(prim.getAsString());
                } catch (NumberFormatException e) {
                    // Molang expression - return NaN to mark as invalid
                    return Float.NaN;
                }
            }
        }
        return 0;
    }

    public static void clearCache() {
        ANIMATION_CACHE.clear();
    }
}
