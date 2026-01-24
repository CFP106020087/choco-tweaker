package com.chocotweak.bedrock.entity;

import com.chocotweak.bedrock.animation.BedrockAnimation;
import com.chocotweak.bedrock.model.BedrockModel;
import com.chocotweak.bedrock.render.BedrockModelCache;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Animation controller for Easter Egg NPC entities.
 * Manages current animation state and transitions.
 */
public class EasterEggAnimController {
    
    // Animation name mapping from YSM extra animations to our states
    private static final Map<EasterEggAnimState, String> YSM_ANIM_MAP = new HashMap<>();
    
    static {
        YSM_ANIM_MAP.put(EasterEggAnimState.IDLE, "idle");
        YSM_ANIM_MAP.put(EasterEggAnimState.WALK, "walk");
        YSM_ANIM_MAP.put(EasterEggAnimState.INTERACT, "interact");
        YSM_ANIM_MAP.put(EasterEggAnimState.REACT_HAPPY, "extra1");  // 打招呼
        YSM_ANIM_MAP.put(EasterEggAnimState.REACT_BOW, "extra2");    // 鼓掌
        YSM_ANIM_MAP.put(EasterEggAnimState.WAVE_HELLO, "extra1");   // 打招呼
        YSM_ANIM_MAP.put(EasterEggAnimState.APPLAUSE, "extra2");     // 鼓掌
        YSM_ANIM_MAP.put(EasterEggAnimState.ACT_CUTE, "extra5");     // 卖萌
    }
    
    private EasterEggAnimState currentState = EasterEggAnimState.IDLE;
    private EasterEggAnimState queuedState = null;
    private float animationTime = 0f;
    private float lastUpdateTime = 0f;
    private boolean isPlayingOnce = false;
    
    private BedrockModel model;
    private Map<String, BedrockAnimation> animations;
    private ResourceLocation textureLocation;
    
    public EasterEggAnimController() {
    }
    
    /**
     * Initialize with model and animation resources.
     */
    public void init(ResourceLocation modelLocation, ResourceLocation animationLocation,
                     ResourceLocation textureLocation) {
        this.model = BedrockModelCache.getInstance().getModel(modelLocation);
        this.animations = BedrockModelCache.getInstance().getAnimations(animationLocation);
        this.textureLocation = textureLocation;
    }
    
    /**
     * Update animation state based on entity behavior.
     * 
     * @param isMoving Whether the entity is currently moving
     * @param worldTime Current world time for animation timing
     */
    public void update(boolean isMoving, float worldTime) {
        float deltaTime = worldTime - lastUpdateTime;
        lastUpdateTime = worldTime;
        
        // Handle state transitions
        if (!isPlayingOnce) {
            if (isMoving && currentState == EasterEggAnimState.IDLE) {
                setState(EasterEggAnimState.WALK);
            } else if (!isMoving && currentState == EasterEggAnimState.WALK) {
                setState(EasterEggAnimState.IDLE);
            }
        }
        
        // Update animation time
        animationTime += deltaTime;
        
        // Check if one-shot animation finished
        if (isPlayingOnce) {
            BedrockAnimation anim = getCurrentAnimation();
            if (anim != null && anim.isFinished(animationTime)) {
                isPlayingOnce = false;
                if (queuedState != null) {
                    setState(queuedState);
                    queuedState = null;
                } else {
                    setState(EasterEggAnimState.IDLE);
                }
            }
        }
    }
    
    /**
     * Set the current animation state.
     */
    public void setState(EasterEggAnimState state) {
        if (currentState != state) {
            currentState = state;
            animationTime = 0f;
        }
    }
    
    /**
     * Play a one-shot animation, then return to idle.
     */
    public void playOnce(EasterEggAnimState state) {
        isPlayingOnce = true;
        queuedState = EasterEggAnimState.IDLE;
        setState(state);
    }
    
    /**
     * Trigger a random reaction animation (for dialog/trade completion).
     */
    public void triggerReaction() {
        playOnce(EasterEggAnimState.getRandomReaction());
    }
    
    /**
     * Get the current animation for rendering.
     */
    public BedrockAnimation getCurrentAnimation() {
        if (animations == null) return null;
        String animName = YSM_ANIM_MAP.getOrDefault(currentState, "idle");
        return animations.get(animName);
    }
    
    public BedrockModel getModel() {
        return model;
    }
    
    public float getAnimationTime() {
        return animationTime;
    }
    
    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }
    
    public EasterEggAnimState getCurrentState() {
        return currentState;
    }
    
    public boolean isInitialized() {
        return model != null && animations != null;
    }
}
