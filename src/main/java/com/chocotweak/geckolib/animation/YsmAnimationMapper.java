package com.chocotweak.geckolib.animation;

import com.chocolate.chocolateQuest.entity.EntityHumanBase;
import net.minecraft.entity.EntityLivingBase;

/**
 * Maps CQ NPC AI states to YSM animation names.
 * This enables automatic animation playback based on NPC behavior.
 */
public class YsmAnimationMapper {

    // Animation names from YSM Wine Fox model
    public static final String IDLE = "idle";
    public static final String WALK = "walk";
    public static final String RUN = "run";
    public static final String JUMP = "jump";
    public static final String SWING_HAND = "swing_hand";
    public static final String ATTACKED = "attacked";
    public static final String RIDE_HORSE = "ride"; // Changed from rideh to ride
    public static final String SLEEP = "sleep";
    public static final String SIT = "sit";
    public static final String BOW = "bow";
    public static final String SNEAK = "sneak";
    public static final String USE_MAINHAND = "use_mainhand";
    public static final String USE_OFFHAND = "use_offhand";
    public static final String SWIM = "swim";
    public static final String CLIMB = "climb";

    // Extra/reaction animations
    public static final String EXTRA_WAVE = "extra1"; // 打招呼
    public static final String EXTRA_APPLAUSE = "extra2"; // 鼓掌
    public static final String EXTRA_GETDOWN = "extra3"; // Get Down
    public static final String EXTRA_DANCE = "extra4"; // 乱舞
    public static final String EXTRA_CUTE = "extra5"; // 卖萌

    // Idle variations
    public static final String[] IDLE_VARIANTS = {
            "idle", "new_idle_1", "new_idle_2", "new_idle_3", "new_idle_4"
    };

    /**
     * Get the appropriate animation name for the current entity state.
     * 
     * @param entity          The entity to check
     * @param limbSwingAmount Movement amount (0 = still, > 0 = moving)
     * @param ticksExisted    For time-based animation selection
     * @return Animation name to play
     */
    public static String getAnimationForState(EntityLivingBase entity, float limbSwingAmount, int ticksExisted) {
        // Check for riding
        if (entity.isRiding()) {
            return RIDE_HORSE;
        }

        // Check for jumping (in air and moving upward)
        if (!entity.onGround && entity.motionY > 0) {
            return JUMP;
        }

        // Check for hurt animation (recent damage)
        if (entity.hurtTime > 0) {
            return ATTACKED;
        }

        // Check CQ-specific states for EntityHumanBase
        if (entity instanceof EntityHumanBase) {
            EntityHumanBase humanBase = (EntityHumanBase) entity;

            // Check for attack swing
            if (humanBase.swingProgress > 0 || humanBase.isSwingInProgress) {
                return SWING_HAND;
            }

            // Check defending/blocking (shield up)
            if (humanBase.isDefending()) {
                return USE_OFFHAND; // Defensive blocking pose
            }

            // Check sleeping state
            if (humanBase.isSleeping()) {
                return SLEEP; // Sleep animation available in YSM
            }

            // Check sitting state
            if (humanBase.isSitting()) {
                return SIT; // Sit animation available in YSM
            }

            // Check speaking state (dialog)
            if (humanBase.isSpeaking()) {
                return EXTRA_WAVE; // Wave or gesture during conversation
            }

            // Check eating state
            if (humanBase.isEating()) {
                return USE_MAINHAND; // Use mainhand animation for eating
            }

            // Check aiming (for ranged weapons)
            if (humanBase.isAiming()) {
                return BOW; // Bow aiming animation
            }
        }

        // Check for swimming
        if (entity.isInWater()) {
            return SWIM;
        }

        // Check for sneaking
        if (entity.isSneaking()) {
            return SNEAK;
        }

        // Movement-based animation
        if (limbSwingAmount > 0.4f) {
            return RUN;
        } else if (limbSwingAmount > 0.01f) {
            return WALK;
        }

        // Idle state - with random reaction animations (cute, tail wag, etc.)
        // 提高卖萌/摇尾巴动画的随机播放概率
        int randomSeed = entity.getEntityId() * 31 + ticksExisted;

        // 每20秒有一次随机播放 reaction 动画的机会（概率提高到 30%）
        if ((ticksExisted % 400) < 10) {
            // 在这个时间窗口内，30% 概率播放 reaction
            if ((randomSeed % 10) < 3) {
                return getRandomReactionAnimation(entity.getEntityId(), ticksExisted);
            }
        }

        // 正常 idle 变体循环
        int idleCycle = (ticksExisted / 60) % IDLE_VARIANTS.length;
        int entitySeed = entity.getEntityId() % IDLE_VARIANTS.length;
        int idleChoice = (idleCycle + entitySeed) % IDLE_VARIANTS.length;
        return IDLE_VARIANTS[idleChoice];
    }

    /**
     * Get the base animation for the current entity state, ignoring attack swing.
     * Used for animation layering - get body movement (walk/run/idle) without arm
     * attacks.
     * 
     * @param entity          The entity to check
     * @param limbSwingAmount Movement amount (0 = still, > 0 = moving)
     * @param ticksExisted    For time-based animation selection
     * @return Animation name to play as base layer
     */
    public static String getBaseAnimationForState(EntityLivingBase entity, float limbSwingAmount, int ticksExisted) {
        // Check for riding
        if (entity.isRiding()) {
            return RIDE_HORSE;
        }

        // Check for jumping (in air and moving upward)
        if (!entity.onGround && entity.motionY > 0) {
            return JUMP;
        }

        // Check for hurt animation (recent damage)
        if (entity.hurtTime > 0) {
            return ATTACKED;
        }

        // Check CQ-specific states for EntityHumanBase (SKIP attack swing check!)
        if (entity instanceof EntityHumanBase) {
            EntityHumanBase humanBase = (EntityHumanBase) entity;

            // Check defending/blocking (shield up)
            if (humanBase.isDefending()) {
                return USE_OFFHAND;
            }

            // Check sleeping state
            if (humanBase.isSleeping()) {
                return SLEEP;
            }

            // Check sitting state
            if (humanBase.isSitting()) {
                return SIT;
            }

            // Check speaking state (dialog)
            if (humanBase.isSpeaking()) {
                return EXTRA_WAVE;
            }

            // Check eating state
            if (humanBase.isEating()) {
                return USE_MAINHAND;
            }

            // Check aiming (for ranged weapons)
            if (humanBase.isAiming()) {
                return BOW;
            }
        }

        // Check for swimming
        if (entity.isInWater()) {
            return SWIM;
        }

        // Check for sneaking
        if (entity.isSneaking()) {
            return SNEAK;
        }

        // Movement-based animation
        if (limbSwingAmount > 0.4f) {
            return RUN;
        } else if (limbSwingAmount > 0.01f) {
            return WALK;
        }

        // Idle state
        int idleCycle = (ticksExisted / 60) % IDLE_VARIANTS.length;
        int entitySeed = entity.getEntityId() % IDLE_VARIANTS.length;
        int idleChoice = (idleCycle + entitySeed) % IDLE_VARIANTS.length;
        return IDLE_VARIANTS[idleChoice];
    }

    /**
     * Get a random reaction animation for dialog/trade completion.
     * 增加更多卖萌/摇尾巴动画，提高播放频率
     */
    public static String getRandomReactionAnimation(int entityId, int ticksExisted) {
        // 扩展动画列表：卖萌、鼓掌、招手、乱舞、摇尾巴（用 cute 多次增加概率）
        String[] reactions = {
                EXTRA_CUTE, // 卖萌 (高概率)
                EXTRA_CUTE, // 卖萌 (高概率)
                EXTRA_WAVE, // 招手
                EXTRA_APPLAUSE, // 鼓掌
                EXTRA_DANCE, // 乱舞/摇尾巴
                EXTRA_CUTE // 卖萌 (高概率)
        };
        int choice = Math.abs((entityId * 17 + ticksExisted / 20)) % reactions.length;
        return reactions[choice];
    }

    /**
     * Determine if the current state should reset animation time.
     * This ensures attack/hurt animations play from start.
     */
    public static boolean shouldResetAnimationTime(String animName) {
        return SWING_HAND.equals(animName) ||
                ATTACKED.equals(animName) ||
                animName.startsWith("extra");
    }

    /**
     * Get animation time for the given state.
     * Some animations should play from entity-relative time, others from absolute
     * time.
     */
    public static float getAnimationTime(EntityLivingBase entity, String animName, float partialTicks) {
        int ticksExisted = entity.ticksExisted;

        if (SWING_HAND.equals(animName)) {
            // Attack animation based on swing progress
            return entity.swingProgress;
        }

        if (ATTACKED.equals(animName)) {
            // Hurt animation based on hurt time
            return (10 - entity.hurtTime) / 10.0f;
        }

        // For idle variants, reset time each cycle for visible animation changes
        if (animName.startsWith("new_idle_") || animName.equals("idle")) {
            int cycleStartTick = (ticksExisted / 60) * 60;
            return ((ticksExisted - cycleStartTick) + partialTicks) / 20.0f;
        }

        // Default: continuous animation time
        return (ticksExisted + partialTicks) / 20.0f;
    }
}
