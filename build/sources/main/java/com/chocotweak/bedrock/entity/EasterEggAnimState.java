package com.chocotweak.bedrock.entity;

/**
 * Animation states for Easter Egg NPCs.
 * Simplified state machine for non-combat entities.
 */
public enum EasterEggAnimState {
    
    /** Standing idle with breathing/idle animation */
    IDLE("idle"),
    
    /** Walking animation (when NPC is set to wander) */
    WALK("walk"),
    
    /** Triggered when player interacts (right-click) */
    INTERACT("interact"),
    
    /** Happy reaction after completing trade/dialog */
    REACT_HAPPY("react_happy"),
    
    /** Bow reaction after completing quest */
    REACT_BOW("react_bow"),
    
    /** Wave hello animation */
    WAVE_HELLO("wave_hello"),
    
    /** Applause animation */
    APPLAUSE("applause"),
    
    /** Cute pose animation */
    ACT_CUTE("act_cute");
    
    private final String animationName;
    
    EasterEggAnimState(String animationName) {
        this.animationName = animationName;
    }
    
    public String getAnimationName() {
        return animationName;
    }
    
    /**
     * Get a random reaction animation for post-dialog/trade.
     */
    public static EasterEggAnimState getRandomReaction() {
        EasterEggAnimState[] reactions = {REACT_HAPPY, REACT_BOW, WAVE_HELLO, APPLAUSE, ACT_CUTE};
        return reactions[(int)(Math.random() * reactions.length)];
    }
}
