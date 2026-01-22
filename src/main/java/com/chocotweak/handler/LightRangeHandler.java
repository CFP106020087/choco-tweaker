package com.chocotweak.handler;

import com.chocolate.chocolateQuest.magic.Elements;
import com.chocotweak.ChocoTweak;
import com.chocotweak.config.CQTweakConfig;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 处理光明元素的攻击距离加成
 * 当玩家切换武器时更新攻击距离
 */
@Mod.EventBusSubscriber(modid = ChocoTweak.MODID)
public class LightRangeHandler {

    private static final UUID LIGHT_RANGE_UUID = UUID.fromString("b5e8d4a2-3c1f-4e9a-8b7d-6c2a1f0e3d5b");
    
    // 记录每个玩家上次持有的武器，用于检测切换
    private static final Map<UUID, ItemStack> lastHeldItems = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!CQTweakConfig.elementEnhance.enabled) {
            return;
        }

        EntityPlayer player = event.player;
        if (player.world.isRemote) {
            return; // 仅服务端处理
        }

        ItemStack currentHeld = player.getHeldItemMainhand();
        ItemStack lastHeld = lastHeldItems.get(player.getUniqueID());

        // 检测是否切换了武器
        boolean changed = false;
        if (lastHeld == null) {
            changed = !currentHeld.isEmpty();
        } else if (!ItemStack.areItemStacksEqual(lastHeld, currentHeld)) {
            changed = true;
        }

        if (changed) {
            updateLightRange(player, currentHeld);
            lastHeldItems.put(player.getUniqueID(), currentHeld.copy());
        }
    }

    private static void updateLightRange(EntityPlayer player, ItemStack weapon) {
        IAttributeInstance reachAttr = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE);
        if (reachAttr == null) {
            return;
        }

        // 移除旧的修改器
        AttributeModifier oldModifier = reachAttr.getModifier(LIGHT_RANGE_UUID);
        if (oldModifier != null) {
            reachAttr.removeModifier(oldModifier);
        }

        // 检查当前武器的光明等级
        if (weapon.isEmpty() || !Elements.hasElements(weapon)) {
            return;
        }

        int lightLevel = Elements.getElementValue(weapon, Elements.light);
        if (lightLevel > 0) {
            double rangeBonus = lightLevel * CQTweakConfig.elementEnhance.lightRangePerLevel;
            AttributeModifier newModifier = new AttributeModifier(
                    LIGHT_RANGE_UUID,
                    "Light Element Range",
                    rangeBonus,
                    0 // 加算
            );
            reachAttr.applyModifier(newModifier);
        }
    }

    /**
     * 玩家登出时清理
     */
    public static void onPlayerLogout(EntityPlayer player) {
        lastHeldItems.remove(player.getUniqueID());
    }
}
