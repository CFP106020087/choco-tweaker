package com.chocotweak.bedrock.entity;

/**
 * Interface for entities that can have Easter Egg behavior.
 * Implemented via Mixin on EntityHumanBase.
 */
public interface IEasterEggCapable {
    
    /**
     * Check if this entity is in Easter Egg mode.
     */
    boolean isEasterEggNpc();
    
    /**
     * Set whether this entity is in Easter Egg mode.
     */
    void setEasterEggNpc(boolean easterEgg);
    
    /**
     * Get the animation controller for Easter Egg rendering.
     */
    EasterEggAnimController getEasterEggController();
    
    /**
     * Get the model variant index (0-14 for wine_fox skins).
     */
    int getModelVariant();
    
    /**
     * Set the model variant index.
     */
    void setModelVariant(int variant);
    
    /**
     * Trigger a reaction animation (call after dialog/trade completion).
     */
    void triggerEasterEggReaction();
}
