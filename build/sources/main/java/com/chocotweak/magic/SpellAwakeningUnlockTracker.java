package com.chocotweak.magic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashSet;
import java.util.Set;

/**
 * 玩家法术觉醒解锁追踪器
 * 
 * 通过任务解锁觉醒：
 * 任务1 → 高速神言
 * 任务2 → 咒文增幅
 * 任务3 → 瞬发咏唱
 * 任务4 → 魔力洪流
 * 任务5 → 远距咏唱
 * 任务6 → 回响之音
 */
public class SpellAwakeningUnlockTracker {

    private static final String NBT_KEY = "ChocoTweak_SpellAwakenings";
    
    // 觉醒ID常量
    public static final String AWAKENING_HIGH_SPEED_CHANT = "highSpeedChant";
    public static final String AWAKENING_SPELL_AMPLIFICATION = "spellAmplification";
    public static final String AWAKENING_INSTANT_CAST = "instantCast";
    public static final String AWAKENING_MANA_SURGE = "manaSurge";
    public static final String AWAKENING_LONG_RANGE = "longRange";
    public static final String AWAKENING_ECHOING_VOICE = "echoingVoice";

    // 觉醒解锁顺序
    public static final String[] UNLOCK_ORDER = {
        AWAKENING_HIGH_SPEED_CHANT,     // 任务1
        AWAKENING_SPELL_AMPLIFICATION,   // 任务2
        AWAKENING_INSTANT_CAST,          // 任务3
        AWAKENING_MANA_SURGE,            // 任务4
        AWAKENING_LONG_RANGE,            // 任务5
        AWAKENING_ECHOING_VOICE          // 任务6
    };

    /**
     * 检查玩家是否解锁了指定觉醒
     */
    public static boolean isUnlocked(EntityPlayer player, String awakeningId) {
        if (player == null) return false;
        
        NBTTagCompound persistentData = getPlayerPersistentData(player);
        if (!persistentData.hasKey(NBT_KEY)) {
            return false;
        }
        
        NBTTagCompound unlocks = persistentData.getCompoundTag(NBT_KEY);
        return unlocks.getBoolean(awakeningId);
    }

    /**
     * 解锁指定觉醒
     */
    public static void unlock(EntityPlayer player, String awakeningId) {
        if (player == null) return;
        
        NBTTagCompound persistentData = getPlayerPersistentData(player);
        NBTTagCompound unlocks;
        
        if (persistentData.hasKey(NBT_KEY)) {
            unlocks = persistentData.getCompoundTag(NBT_KEY);
        } else {
            unlocks = new NBTTagCompound();
        }
        
        unlocks.setBoolean(awakeningId, true);
        persistentData.setTag(NBT_KEY, unlocks);
        
        System.out.println("[ChocoTweak] Player " + player.getName() + " unlocked awakening: " + awakeningId);
    }

    /**
     * 解锁下一个觉醒（按顺序）
     * @return 解锁的觉醒ID，如果全部解锁则返回null
     */
    public static String unlockNext(EntityPlayer player) {
        for (String awakeningId : UNLOCK_ORDER) {
            if (!isUnlocked(player, awakeningId)) {
                unlock(player, awakeningId);
                return awakeningId;
            }
        }
        return null; // 全部解锁
    }

    /**
     * 获取已解锁的觉醒数量
     */
    public static int getUnlockedCount(EntityPlayer player) {
        int count = 0;
        for (String awakeningId : UNLOCK_ORDER) {
            if (isUnlocked(player, awakeningId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 解锁所有觉醒（调试用）
     */
    public static void unlockAll(EntityPlayer player) {
        for (String awakeningId : UNLOCK_ORDER) {
            unlock(player, awakeningId);
        }
    }

    /**
     * 重置所有觉醒解锁状态
     */
    public static void resetAll(EntityPlayer player) {
        if (player == null) return;
        
        NBTTagCompound persistentData = getPlayerPersistentData(player);
        persistentData.removeTag(NBT_KEY);
    }

    /**
     * 获取玩家持久化数据
     */
    private static NBTTagCompound getPlayerPersistentData(EntityPlayer player) {
        // 使用 Forge 的持久化数据
        return player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }
}
