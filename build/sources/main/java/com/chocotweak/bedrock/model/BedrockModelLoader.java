package com.chocotweak.bedrock.model;

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

/**
 * Loads Bedrock 1.12.0 geometry format JSON files into BedrockModel objects.
 */
public class BedrockModelLoader {
    
    private static final Logger LOGGER = LogManager.getLogger("BedrockModelLoader");
    
    /**
     * Load a Bedrock model from a resource location.
     * 
     * @param location The resource location of the JSON file
     * @return The loaded BedrockModel, or null if loading failed
     */
    public static BedrockModel load(ResourceLocation location) {
        try {
            InputStream stream = Minecraft.getMinecraft().getResourceManager()
                    .getResource(location).getInputStream();
            return load(stream);
        } catch (Exception e) {
            LOGGER.error("Failed to load Bedrock model from {}: {}", location, e.getMessage());
            return null;
        }
    }
    
    /**
     * Load a Bedrock model from an input stream.
     */
    public static BedrockModel load(InputStream stream) {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            return parseModel(root);
        } catch (Exception e) {
            LOGGER.error("Failed to parse Bedrock model: {}", e.getMessage());
            return null;
        }
    }
    
    private static BedrockModel parseModel(JsonObject root) {
        // Check format version
        String formatVersion = root.has("format_version") ? 
                root.get("format_version").getAsString() : "unknown";
        LOGGER.debug("Loading Bedrock model format version: {}", formatVersion);
        
        // Get the geometry array
        if (!root.has("minecraft:geometry")) {
            LOGGER.error("Invalid Bedrock model: missing 'minecraft:geometry' key");
            return null;
        }
        
        JsonArray geometryArray = root.getAsJsonArray("minecraft:geometry");
        if (geometryArray.size() == 0) {
            LOGGER.error("Invalid Bedrock model: empty geometry array");
            return null;
        }
        
        // Parse the first geometry (models usually have only one)
        JsonObject geometry = geometryArray.get(0).getAsJsonObject();
        return parseGeometry(geometry);
    }
    
    private static BedrockModel parseGeometry(JsonObject geometry) {
        // Parse description
        JsonObject description = geometry.getAsJsonObject("description");
        String identifier = description.has("identifier") ? 
                description.get("identifier").getAsString() : "geometry.unknown";
        int textureWidth = description.has("texture_width") ? 
                description.get("texture_width").getAsInt() : 64;
        int textureHeight = description.has("texture_height") ? 
                description.get("texture_height").getAsInt() : 64;
        
        BedrockModel model = new BedrockModel(identifier, textureWidth, textureHeight);
        
        // Parse bones
        if (geometry.has("bones")) {
            JsonArray bonesArray = geometry.getAsJsonArray("bones");
            for (JsonElement boneElement : bonesArray) {
                BedrockBone bone = new BedrockBone(
                        boneElement.getAsJsonObject(), 
                        textureWidth, 
                        textureHeight
                );
                model.addBone(bone);
            }
        }
        
        // Build the hierarchy
        model.buildHierarchy();
        
        LOGGER.info("Loaded Bedrock model '{}' with {} bones", 
                identifier, model.boneMap.size());
        
        return model;
    }
}
