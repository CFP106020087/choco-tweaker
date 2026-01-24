package com.chocotweak.geckolib.loader;

import com.chocotweak.geckolib.render.built.CubeUV;
import com.chocotweak.geckolib.render.built.FaceUV;
import com.chocotweak.geckolib.render.built.GeoBone;
import com.chocotweak.geckolib.render.built.GeoCube;
import com.chocotweak.geckolib.render.built.GeoModel;
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
 * Loads Bedrock geometry JSON into GeoModel objects.
 * Backport from YSM GeckoLib with YSM coordinate transforms.
 */
public class GeoModelLoader {

    private static final Logger LOGGER = LogManager.getLogger("GeoModelLoader");
    private static final Map<ResourceLocation, GeoModel> MODEL_CACHE = new HashMap<>();

    public static GeoModel load(ResourceLocation location) {
        if (MODEL_CACHE.containsKey(location)) {
            return MODEL_CACHE.get(location);
        }

        try {
            InputStream stream = Minecraft.getMinecraft().getResourceManager()
                    .getResource(location).getInputStream();
            GeoModel model = load(stream);
            if (model != null) {
                MODEL_CACHE.put(location, model);
            }
            return model;
        } catch (Exception e) {
            LOGGER.error("Failed to load GeoModel from {}: {}", location, e.getMessage());
            return null;
        }
    }

    public static GeoModel load(InputStream stream) {
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            return parseModel(root);
        } catch (Exception e) {
            LOGGER.error("Failed to parse GeoModel: {}", e.getMessage());
            return null;
        }
    }

    private static GeoModel parseModel(JsonObject root) {
        // Check for minecraft:geometry array (Bedrock 1.12.0 format)
        if (!root.has("minecraft:geometry")) {
            LOGGER.error("Missing 'minecraft:geometry' key");
            return null;
        }

        JsonArray geometryArray = root.getAsJsonArray("minecraft:geometry");
        if (geometryArray.size() == 0) {
            return null;
        }

        JsonObject geometry = geometryArray.get(0).getAsJsonObject();

        // Parse description
        JsonObject description = geometry.getAsJsonObject("description");
        String identifier = description.has("identifier") ? description.get("identifier").getAsString()
                : "geometry.unknown";
        int textureWidth = description.has("texture_width") ? description.get("texture_width").getAsInt() : 64;
        int textureHeight = description.has("texture_height") ? description.get("texture_height").getAsInt() : 64;

        GeoModel model = new GeoModel(identifier, textureWidth, textureHeight);

        // Parse bones
        if (geometry.has("bones")) {
            JsonArray bonesArray = geometry.getAsJsonArray("bones");
            Map<String, GeoBone> boneMap = new HashMap<>();

            // First pass: create all bones
            for (JsonElement boneElement : bonesArray) {
                JsonObject boneJson = boneElement.getAsJsonObject();
                GeoBone bone = parseBone(boneJson, textureWidth, textureHeight);
                boneMap.put(bone.name, bone);
                model.addBone(bone);
            }

            // Second pass: build hierarchy
            for (JsonElement boneElement : bonesArray) {
                JsonObject boneJson = boneElement.getAsJsonObject();
                String boneName = boneJson.get("name").getAsString();
                GeoBone bone = boneMap.get(boneName);

                if (boneJson.has("parent")) {
                    String parentName = boneJson.get("parent").getAsString();
                    GeoBone parent = boneMap.get(parentName);
                    if (parent != null) {
                        bone.parent = parent;
                        parent.childBones.add(bone);
                    } else {
                        model.addTopLevelBone(bone);
                    }
                } else {
                    model.addTopLevelBone(bone);
                }
            }
        }

        LOGGER.info("Loaded GeoModel '{}' with {} bones", identifier, model.boneMap.size());
        return model;
    }

    private static GeoBone parseBone(JsonObject json, int textureWidth, int textureHeight) {
        String name = json.get("name").getAsString();

        // Parse pivot with YSM transform: -X, Y, Z
        float pivotX = 0, pivotY = 0, pivotZ = 0;
        if (json.has("pivot")) {
            JsonArray pivotArr = json.getAsJsonArray("pivot");
            pivotX = -pivotArr.get(0).getAsFloat(); // Negate X
            pivotY = pivotArr.get(1).getAsFloat();
            pivotZ = pivotArr.get(2).getAsFloat();
        }

        // Parse rotation with YSM transform: -X, -Y, Z (radians)
        float rotX = 0, rotY = 0, rotZ = 0;
        if (json.has("rotation")) {
            JsonArray rotArr = json.getAsJsonArray("rotation");
            rotX = (float) Math.toRadians(-rotArr.get(0).getAsFloat()); // Negate X
            rotY = (float) Math.toRadians(-rotArr.get(1).getAsFloat()); // Negate Y
            rotZ = (float) Math.toRadians(rotArr.get(2).getAsFloat());
        }

        GeoBone bone = new GeoBone(name, pivotX, pivotY, pivotZ, rotX, rotY, rotZ);

        // Parse cubes
        if (json.has("cubes")) {
            JsonArray cubesArray = json.getAsJsonArray("cubes");
            for (JsonElement cubeElement : cubesArray) {
                JsonObject cubeJson = cubeElement.getAsJsonObject();
                GeoCube cube = parseCube(cubeJson, textureWidth, textureHeight);
                if (cube != null) {
                    bone.childCubes.add(cube);
                }
            }
        }

        return bone;
    }

    private static GeoCube parseCube(JsonObject json, int textureWidth, int textureHeight) {
        double[] origin = parseVec3(json, "origin", new double[] { 0, 0, 0 });
        double[] size = parseVec3(json, "size", new double[] { 1, 1, 1 });
        double[] rotation = parseVec3(json, "rotation", null);
        double[] pivot = parseVec3(json, "pivot", null);
        double inflate = json.has("inflate") ? json.get("inflate").getAsDouble() : 0;
        Boolean mirror = json.has("mirror") ? json.get("mirror").getAsBoolean() : null;

        // Parse UV - can be array (box UV) or object (per-face UV)
        CubeUV cubeUV;
        if (json.has("uv")) {
            JsonElement uvElement = json.get("uv");
            if (uvElement.isJsonArray()) {
                // Box UV: [u, v]
                JsonArray uvArr = uvElement.getAsJsonArray();
                double[] boxUV = new double[] {
                        uvArr.get(0).getAsDouble(),
                        uvArr.get(1).getAsDouble()
                };
                cubeUV = new CubeUV(boxUV);
            } else if (uvElement.isJsonObject()) {
                // Per-face UV: {north: {uv: [...], uv_size: [...]}, ...}
                JsonObject uvObj = uvElement.getAsJsonObject();
                cubeUV = new CubeUV(
                        parseFaceUV(uvObj, "north"),
                        parseFaceUV(uvObj, "south"),
                        parseFaceUV(uvObj, "east"),
                        parseFaceUV(uvObj, "west"),
                        parseFaceUV(uvObj, "up"),
                        parseFaceUV(uvObj, "down"));
            } else {
                cubeUV = new CubeUV(new double[] { 0, 0 });
            }
        } else {
            cubeUV = new CubeUV(new double[] { 0, 0 });
        }

        return GeoCube.create(origin, size, rotation, pivot, cubeUV,
                textureWidth, textureHeight, inflate, mirror);
    }

    private static FaceUV parseFaceUV(JsonObject uvObj, String faceName) {
        if (!uvObj.has(faceName))
            return null;
        JsonObject faceObj = uvObj.getAsJsonObject(faceName);
        double[] uv = parseVec2(faceObj, "uv", new double[] { 0, 0 });
        double[] uvSize = parseVec2(faceObj, "uv_size", new double[] { 0, 0 });
        return new FaceUV(uv, uvSize);
    }

    private static double[] parseVec3(JsonObject json, String key, double[] defaultValue) {
        if (!json.has(key))
            return defaultValue;
        JsonElement element = json.get(key);
        if (!element.isJsonArray())
            return defaultValue;
        JsonArray arr = element.getAsJsonArray();
        if (arr.size() < 3)
            return defaultValue;
        return new double[] {
                arr.get(0).getAsDouble(),
                arr.get(1).getAsDouble(),
                arr.get(2).getAsDouble()
        };
    }

    private static double[] parseVec2(JsonObject json, String key, double[] defaultValue) {
        if (!json.has(key))
            return defaultValue;
        JsonElement element = json.get(key);
        if (!element.isJsonArray())
            return defaultValue;
        JsonArray arr = element.getAsJsonArray();
        if (arr.size() < 2)
            return defaultValue;
        return new double[] {
                arr.get(0).getAsDouble(),
                arr.get(1).getAsDouble()
        };
    }

    public static void clearCache() {
        MODEL_CACHE.clear();
    }
}
