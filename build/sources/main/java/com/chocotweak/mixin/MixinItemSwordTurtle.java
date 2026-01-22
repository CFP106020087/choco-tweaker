package com.chocotweak.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

/**
 * ItemSwordTurtle专用Mixin
 * 使用@Overwrite和SRG名func_111205_h覆盖getItemAttributeModifiers
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.items.swords.ItemSwordTurtle", remap = false)
public class MixinItemSwordTurtle {

    private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final UUID KNOCKBACK_RESIST_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CE");

    static {
        System.out.println("[ChocoTweak] MixinItemSwordTurtle class loaded!");
    }

    /**
     * @author ChocoTweak
     * @reason 修改龟盾攻速为极慢
     */
    @Overwrite
    public Multimap<String, AttributeModifier> func_111205_h(EntityEquipmentSlot slot) {
        System.out.println("[ChocoTweak] TurtleSword func_111205_h called!");

        Multimap<String, AttributeModifier> map = HashMultimap.create();

        if (slot == EntityEquipmentSlot.MAINHAND) {
            // 伤害: 25
            map.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 25.0, 0));

            // 攻速: -3.2 (0.8攻速，很慢)
            map.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3.2, 0));

            // 击退抗性
            map.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
                    new AttributeModifier(KNOCKBACK_RESIST_UUID, "Weapon modifier", 1.0, 0));
        }

        return map;
    }
}


