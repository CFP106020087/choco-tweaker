package com.chocotweak.bedrock.handler;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import com.chocotweak.bedrock.entity.IEasterEggCapable;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles dialog/trade completion events to trigger reaction animations on
 * Easter Egg NPCs.
 * 
 * Note: This handler monitors EntityHumanBase interactions and triggers
 * animations.
 * In a full implementation, this would hook into CQ's dialog system events.
 */
@Mod.EventBusSubscriber
public class DialogReactionHandler {

    /**
     * Trigger a reaction animation on an Easter Egg NPC.
     * Call this method when a dialog/trade is completed.
     * 
     * @param entity The EntityHumanBase that completed a dialog
     */
    public static void onDialogComplete(EntityHumanBase entity) {
        if (entity instanceof IEasterEggCapable) {
            IEasterEggCapable easterEgg = (IEasterEggCapable) entity;
            if (easterEgg.isEasterEggNpc()) {
                easterEgg.triggerEasterEggReaction();
            }
        }
    }

    /**
     * Trigger a reaction animation after a successful trade.
     * 
     * @param entity The EntityHumanBase that completed a trade
     */
    public static void onTradeComplete(EntityHumanBase entity) {
        onDialogComplete(entity); // Same behavior for now
    }
}
