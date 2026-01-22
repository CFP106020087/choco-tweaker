package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.quest.worldManager.TerrainManager;
import com.chocotweak.ChocoTweak;
import com.chocotweak.config.CQTweakConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Mixin to modify dungeon generation settings
 * 修改地牢生成设置
 * 
 * 功能：
 * - spawnChanceMultiplier < 1.0: 减少地牢生成
 * - spawnChanceMultiplier > 1.0: 增加地牢生成（在相邻网格点生成）
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.WorldGeneratorNew", remap = false)
public abstract class MixinWorldGeneratorNew {

    /**
     * Shadow引用原方法，用于递归调用
     */
    @Shadow
    public abstract void generateBigDungeon(net.minecraft.world.World world, Random random, int i, int k,
            boolean addDungeon);

    /**
     * 防止无限递归的深度计数器
     */
    @Unique
    private static final ThreadLocal<Integer> chocotweak$recursionDepth = ThreadLocal.withInitial(() -> 0);

    /**
     * 在地牢生成开始前检查是否启用
     */
    @Inject(method = "generateBigDungeon", at = @At("HEAD"), cancellable = true)
    private void onGenerateBigDungeon(net.minecraft.world.World world, Random random, int i, int k,
            boolean addDungeon, CallbackInfo ci) {
        try {
            // 检查是否禁用地牢生成
            if (!CQTweakConfig.dungeons.enabled) {
                ci.cancel();
                return;
            }

            double multiplier = CQTweakConfig.dungeons.spawnChanceMultiplier;

            // 倍率 < 1.0: 有几率跳过生成
            if (multiplier < 1.0) {
                double skipChance = 1.0 - multiplier;
                if (random.nextDouble() < skipChance) {
                    ci.cancel();
                    return;
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 在地牢生成完成后，如果倍率>1.0，在相邻网格点额外生成地牢
     * 使用CQ的网格对齐，保证不会违反dungeonSeparation规则
     */
    @Inject(method = "generateBigDungeon", at = @At("TAIL"))
    private void onGenerateBigDungeonTail(net.minecraft.world.World world, Random random, int i, int k,
            boolean addDungeon, CallbackInfo ci) {
        try {
            // 防止无限递归：只在深度0时触发额外生成
            int depth = chocotweak$recursionDepth.get();
            if (depth > 0) {
                return;
            }

            double multiplier = CQTweakConfig.dungeons.spawnChanceMultiplier;

            // 倍率 > 1.0: 尝试额外生成
            if (multiplier > 1.0 && addDungeon) {
                chocotweak$recursionDepth.set(depth + 1);
                try {
                    // 获取CQ地牢间距(默认10区块)
                    int separation = TerrainManager.getTerritorySeparation();
                    int separationBlocks = separation * 16;

                    double extraChance = multiplier - 1.0;
                    int extraAttempts = 0;
                    final int MAX_EXTRA = 4;

                    // 相邻网格点方向 (上下左右)
                    int[][] directions = {
                            { -separationBlocks, 0 }, // 西
                            { separationBlocks, 0 }, // 东
                            { 0, -separationBlocks }, // 北
                            { 0, separationBlocks } // 南
                    };

                    while (extraChance > 0 && extraAttempts < MAX_EXTRA) {
                        if (extraChance >= 1.0 || random.nextDouble() < extraChance) {
                            // 随机选择一个方向
                            int[] dir = directions[random.nextInt(4)];
                            int newI = i + dir[0];
                            int newK = k + dir[1];

                            // 创建独立的Random
                            Random newRandom = new Random(world.getSeed() + newI * 31L + newK);

                            ChocoTweak.LOGGER.info(
                                    "[ChocoTweak] Extra dungeon at grid ({}, {}), separation={}",
                                    newI / 16, newK / 16, separation);

                            // 递归调用原方法，生成在网格点
                            generateBigDungeon(world, newRandom, newI, newK, true);
                            extraAttempts++;
                        }
                        extraChance -= 1.0;
                    }
                } finally {
                    chocotweak$recursionDepth.set(depth);
                }
            }
        } catch (Exception e) {
            ChocoTweak.LOGGER.warn("[ChocoTweak] Error in extra dungeon generation", e);
        }
    }
}
