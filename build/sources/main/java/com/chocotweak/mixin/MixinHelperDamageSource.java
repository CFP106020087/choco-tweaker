package com.chocotweak.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * 修改CQ伤害源
 * 1. 魔法伤害使用专属 "cq_spell" 类型
 * 2. 物理伤害保持玩家伤害吃增伤
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.utils.HelperDamageSource", remap = false)
public class MixinHelperDamageSource {

    /** CQ法术专属伤害类型名称 */
    public static final String CQ_SPELL_DAMAGE_TYPE = "cq_spell";

    /**
     * 投射物魔法伤害 -> cq_spell
     * 
     * @author ChocoTweak
     * @reason 使用专属伤害类型，法袍可以识别
     */
    @Overwrite
    public static DamageSource causeProjectileMagicDamage(Entity projectile, Entity shooter) {
        // 所有CQ魔法投射物使用 "cq_spell" 类型
        return (new EntityDamageSourceIndirect(CQ_SPELL_DAMAGE_TYPE, projectile, shooter))
                .setMagicDamage()
                .setProjectile();
    }

    /**
     * 直接魔法伤害 -> cq_spell
     * 
     * @author ChocoTweak
     * @reason 使用专属伤害类型，法袍可以识别
     */
    @Overwrite
    public static DamageSource causeMagicDamage(Entity shooter) {
        return (new EntityDamageSource(CQ_SPELL_DAMAGE_TYPE, shooter))
                .setMagicDamage();
    }

    /**
     * 投射物物理伤害 -> 玩家伤害 (枪械等)
     * 
     * @author ChocoTweak
     * @reason 让枪械能触发玩家增伤
     */
    @Overwrite
    public static DamageSource causeProjectilePhysicalDamage(Entity projectile, Entity shooter) {
        if (shooter instanceof EntityPlayer) {
            return (new EntityDamageSourceIndirect("player", projectile, shooter)).setProjectile();
        }
        return (new EntityDamageSourceIndirect("generic", projectile, shooter)).setProjectile();
    }

    /**
     * 直接物理伤害 -> 玩家伤害 (Golem武器)
     * 
     * @author ChocoTweak
     * @reason 让Golem武器能触发玩家增伤
     */
    @Overwrite
    public static DamageSource causePhysicalDamage(Entity shooter) {
        if (shooter instanceof EntityPlayer) {
            return DamageSource.causePlayerDamage((EntityPlayer) shooter);
        }
        return new EntityDamageSource("generic", shooter);
    }

    /**
     * 检查伤害源是否是CQ法术
     */
    public static boolean isCQSpellDamage(DamageSource source) {
        return CQ_SPELL_DAMAGE_TYPE.equals(source.getDamageType());
    }
}
