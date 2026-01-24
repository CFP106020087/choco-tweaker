package com.chocotweak.bedrock.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
 * Loads Bedrock 1.8.0 animation format JSON files.
 */
public class BedrockAnimationLoader {
    
    private static final Logger LOGGER = LogManager.getLogger("BedrockAnimationLoader");
    
    /**
     * Load animations from a resource location.
     * 
     * @param location The resource location of the animation JSON file
     * @return Map of animation name to BedrockAnimation, or empty map if loading failed
     */
    public static Map<String, BedrockAnimation> load(ResourceLocation location) {
        try {
            InputStream stream = Minecraft.getMinecraft().getResourceManager()
                    .getResource(location).getInputStream();
            return load(stream);
        } catch (Exception e) {
            LOGGER.error("Failed to load Bedrock animations from {}: {}", location, e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * Load animations from an input stream.
     */
    public static Map<String, BedrockAnimation> load(InputStream stream) {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            return parseAnimations(root);
        } catch (Exception e) {
            LOGGER.error("Failed to parse Bedrock animations: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    private static Map<String, BedrockAnimation> parseAnimations(JsonObject root) {
        Map<String, BedrockAnimation> result = new HashMap<>();
        
        // Check format version
        String formatVersion = root.has("format_version") ? 
                root.get("format_version").getAsString() : "unknown";
        LOGGER.debug("Loading Bedrock animation format version: {}", formatVersion);
        
        if (!root.has("animations")) {
            LOGGER.warn("No 'animations' key found in animation file");
            return result;
        }
        
        JsonObject animations = root.getAsJsonObject("animations");
        for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
            String animName = entry.getKey();
            JsonObject animJson = entry.getValue().getAsJsonObject();
            
            BedrockAnimation anim = parseAnimation(animName, animJson);
            if (anim != null) {
                result.put(animName, anim);
            }
        }
        
        LOGGER.info("Loaded {} Bedrock animations", result.size());
        return result;
    }
    
    private static BedrockAnimation parseAnimation(String name, JsonObject json) {
        boolean loop = json.has("loop") && json.get("loop").getAsBoolean();
        float length = json.has("animation_length") ? 
                json.get("animation_length").getAsFloat() : 0f;
        
        BedrockAnimation animation = new BedrockAnimation(name, loop, length);
        
        if (json.has("bones")) {
            JsonObject bones = json.getAsJsonObject("bones");
            for (Map.Entry<String, JsonElement> boneEntry : bones.entrySet()) {
                String boneName = boneEntry.getKey();
                JsonObject boneJson = boneEntry.getValue().getAsJsonObject();
                
                BedrockBoneAnimation boneAnim = parseBoneAnimation(boneName, boneJson);
                animation.addBoneAnimation(boneAnim);
            }
        }
        
        return animation;
    }
    
    private static BedrockBoneAnimation parseBoneAnimation(String boneName, JsonObject json) {
        BedrockBoneAnimation boneAnim = new BedrockBoneAnimation(boneName);
        
        // Parse rotation keyframes
        float[] staticRotation = null;
        Map<Float, float[]> rotationKeyframes = new HashMap<>();
        if (json.has("rotation")) {
            JsonElement rotElement = json.get("rotation");
            if (rotElement.isJsonArray()) {
                // Static rotation
                staticRotation = parseVec3(rotElement.getAsJsonArray());
            } else if (rotElement.isJsonObject()) {
                // Keyframed rotation
                rotationKeyframes = parseKeyframedVec3(rotElement.getAsJsonObject());
            }
        }
        
        // Parse position keyframes
        float[] staticPosition = null;
        Map<Float, float[]> positionKeyframes = new HashMap<>();
        if (json.has("position")) {
            JsonElement posElement = json.get("position");
            if (posElement.isJsonArray()) {
                staticPosition = parseVec3(posElement.getAsJsonArray());
            } else if (posElement.isJsonObject()) {
                positionKeyframes = parseKeyframedVec3(posElement.getAsJsonObject());
            }
        }
        
        // Parse scale keyframes
        float[] staticScale = null;
        Map<Float, float[]> scaleKeyframes = new HashMap<>();
        if (json.has("scale")) {
            JsonElement scaleElement = json.get("scale");
            if (scaleElement.isJsonArray()) {
                staticScale = parseVec3(scaleElement.getAsJsonArray());
            } else if (scaleElement.isJsonObject()) {
                scaleKeyframes = parseKeyframedVec3(scaleElement.getAsJsonObject());
            } else if (scaleElement.isJsonPrimitive()) {
                // Uniform scale
                float s = scaleElement.getAsFloat();
                staticScale = new float[]{s, s, s};
            }
        }
        
        // If all static, create a single keyframe at t=0
        if (rotationKeyframes.isEmpty() && positionKeyframes.isEmpty() && scaleKeyframes.isEmpty()) {
            boneAnim.addKeyframe(new BedrockKeyframe(0, staticRotation, staticPosition, staticScale));
        } else {
            // Merge all keyframe timestamps
            java.util.Set<Float> allTimes = new java.util.TreeSet<>();
            allTimes.addAll(rotationKeyframes.keySet());
            allTimes.addAll(positionKeyframes.keySet());
            allTimes.addAll(scaleKeyframes.keySet());
            
            for (float time : allTimes) {
                float[] rot = rotationKeyframes.getOrDefault(time, staticRotation);
                float[] pos = positionKeyframes.getOrDefault(time, staticPosition);
                float[] scale = scaleKeyframes.getOrDefault(time, staticScale);
                boneAnim.addKeyframe(new BedrockKeyframe(time, rot, pos, scale));
            }
        }
        
        return boneAnim;
    }
    
    private static float[] parseVec3(JsonArray arr) {
        if (arr.size() < 3) return null;
        return new float[]{
            arr.get(0).getAsFloat(),
            arr.get(1).getAsFloat(),
            arr.get(2).getAsFloat()
        };
    }
    
    private static Map<Float, float[]> parseKeyframedVec3(JsonObject keyframes) {
        Map<Float, float[]> result = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : keyframes.entrySet()) {
            try {
                float time = Float.parseFloat(entry.getKey());
                JsonElement value = entry.getValue();
                
                float[] vec = null;
                if (value.isJsonArray()) {
                    vec = parseVec3(value.getAsJsonArray());
                } else if (value.isJsonObject()) {
                    // Handle complex keyframe format with lerp_mode etc
                    JsonObject kfObj = value.getAsJsonObject();
                    if (kfObj.has("vector")) {
                        vec = parseVec3(kfObj.getAsJsonArray("vector"));
                    } else if (kfObj.has("post")) {
                        // Use post value
                        vec = parseVec3(kfObj.getAsJsonArray("post"));
                    }
                }
                
                if (vec != null) {
                    result.put(time, vec);
                }
            } catch (NumberFormatException e) {
                // Skip non-numeric keys (like Molang expressions)
            }
        }
        return result;
    }
}
