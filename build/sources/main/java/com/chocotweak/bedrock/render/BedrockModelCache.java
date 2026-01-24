package com.chocotweak.bedrock.render;

import com.chocotweak.bedrock.animation.BedrockAnimation;
import com.chocotweak.bedrock.animation.BedrockAnimationLoader;
import com.chocotweak.bedrock.model.BedrockModel;
import com.chocotweak.bedrock.model.BedrockModelLoader;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches loaded Bedrock models and animations to avoid redundant loading.
 */
public class BedrockModelCache {
    
    private static final Logger LOGGER = LogManager.getLogger("BedrockModelCache");
    private static final BedrockModelCache INSTANCE = new BedrockModelCache();
    
    private final Map<ResourceLocation, BedrockModel> modelCache = new HashMap<>();
    private final Map<ResourceLocation, Map<String, BedrockAnimation>> animationCache = new HashMap<>();
    
    private BedrockModelCache() {}
    
    public static BedrockModelCache getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get or load a Bedrock model.
     */
    public BedrockModel getModel(ResourceLocation location) {
        if (modelCache.containsKey(location)) {
            return modelCache.get(location);
        }
        
        BedrockModel model = BedrockModelLoader.load(location);
        if (model != null) {
            modelCache.put(location, model);
            LOGGER.debug("Cached model: {}", location);
        }
        return model;
    }
    
    /**
     * Get or load Bedrock animations.
     */
    public Map<String, BedrockAnimation> getAnimations(ResourceLocation location) {
        if (animationCache.containsKey(location)) {
            return animationCache.get(location);
        }
        
        Map<String, BedrockAnimation> anims = BedrockAnimationLoader.load(location);
        if (!anims.isEmpty()) {
            animationCache.put(location, anims);
            LOGGER.debug("Cached {} animations from: {}", anims.size(), location);
        }
        return anims;
    }
    
    /**
     * Get a specific animation by name from a resource.
     */
    public BedrockAnimation getAnimation(ResourceLocation location, String animationName) {
        Map<String, BedrockAnimation> anims = getAnimations(location);
        return anims.get(animationName);
    }
    
    /**
     * Clear all caches (useful for resource pack reloads).
     */
    public void clearCache() {
        modelCache.clear();
        animationCache.clear();
        LOGGER.info("Bedrock model cache cleared");
    }
    
    /**
     * Get cache statistics for debugging.
     */
    public String getCacheStats() {
        int totalAnims = animationCache.values().stream()
                .mapToInt(Map::size).sum();
        return String.format("Models: %d, Animation Files: %d, Total Animations: %d",
                modelCache.size(), animationCache.size(), totalAnims);
    }
}
