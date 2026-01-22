package com.chocotweak.handler;

import com.chocotweak.api.IShieldAttackItem;
import com.chocotweak.network.ChocoNetwork;
import com.chocotweak.network.PacketShieldAttack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * 盾牌攻击处理器
 * 
 * 注册方式：
 * MinecraftForge.EVENT_BUS.register(ShieldAttackHandler.INSTANCE);
 * ChocoNetwork.init(); // 在 preInit 中
 * 
 * 原理：
 * 1. 客户端检测左键按下，发包到服务端
 * 2. 服务端记录玩家的左键状态
 * 3. 服务端 PlayerTickEvent 中检测状态并执行攻击
 * 
 * 注意：使用类名检查代替直接类引用，避免触发CQ类加载失败
 */
public class ShieldAttackHandler {

    public static final ShieldAttackHandler INSTANCE = new ShieldAttackHandler();

    /** 是否启用 */
    public static boolean enabled = true;

    /** 攻击冷却 (tick) */
    public static int attackCooldown = 10;

    // 服务端：玩家左键状态
    private static final Map<UUID, Boolean> leftClickStates = new WeakHashMap<>();

    // 服务端：玩家攻击冷却
    private static final Map<UUID, Integer> cooldowns = new WeakHashMap<>();

    // 客户端状态持有者 - 避免服务端 NoSuchFieldError
    @SideOnly(Side.CLIENT)
    private static class ClientState {
        static boolean lastLeftClickDown = false;
    }

    private ShieldAttackHandler() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 安全检测物品是否为 ItemSwordAndShieldBase 子类
     */
    private static boolean isSwordAndShield(Item item) {
        Class<?> clazz = item.getClass();
        while (clazz != null) {
            if (clazz.getName().equals("com.chocolate.chocolateQuest.items.swords.ItemSwordAndShieldBase")) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    // ==================== 服务端 API ====================

    /**
     * 设置玩家左键状态（由网络包调用）
     */
    public static void setLeftClickState(EntityPlayerMP player, boolean down) {
        leftClickStates.put(player.getUniqueID(), down);
    }

    /**
     * 获取玩家左键状态
     */
    public static boolean isLeftClickDown(EntityPlayer player) {
        return leftClickStates.getOrDefault(player.getUniqueID(), false);
    }

    // ==================== 事件处理 ====================

    /**
     * 客户端：检测左键并发包
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (!enabled)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.isGamePaused()) {
            ClientState.lastLeftClickDown = false;
            return;
        }

        // 检查是否正在格挡
        if (!mc.player.isActiveItemStackBlocking()) {
            ClientState.lastLeftClickDown = false;
            return;
        }

        // 检查是否持有可攻击的盾牌/剑盾
        if (!isBlockableItem(mc.player.getActiveItemStack())) {
            ClientState.lastLeftClickDown = false;
            return;
        }

        // 检测左键状态
        boolean leftClickDown = mc.gameSettings.keyBindAttack.isKeyDown();

        // 边沿检测：只在按下瞬间发包
        if (leftClickDown && !ClientState.lastLeftClickDown) {
            ChocoNetwork.INSTANCE.sendToServer(new PacketShieldAttack(true));
        }

        ClientState.lastLeftClickDown = leftClickDown;
    }

    /**
     * 服务端：处理攻击逻辑
     */
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        if (event.side != Side.SERVER)
            return;
        if (!enabled)
            return;

        EntityPlayer player = event.player;
        if (!(player instanceof EntityPlayerMP))
            return;

        EntityPlayerMP mp = (EntityPlayerMP) player;
        UUID uuid = mp.getUniqueID();

        // 更新冷却
        int cd = cooldowns.getOrDefault(uuid, 0);
        if (cd > 0) {
            cooldowns.put(uuid, cd - 1);
        }

        // 检查左键状态
        if (!isLeftClickDown(player))
            return;

        // 消费左键状态
        leftClickStates.put(uuid, false);

        // 检查冷却
        if (cd > 0)
            return;

        // 检查是否正在格挡
        if (!player.isActiveItemStackBlocking())
            return;

        ItemStack activeStack = player.getActiveItemStack();
        if (!isBlockableItem(activeStack))
            return;

        // 执行攻击
        performAttack(mp, activeStack);

        // 设置冷却
        cooldowns.put(uuid, attackCooldown);
    }

    /**
     * 执行攻击
     */
    private void performAttack(EntityPlayerMP player, ItemStack activeStack) {
        // 如果物品实现了 IShieldAttackItem，使用自定义逻辑
        if (activeStack.getItem() instanceof IShieldAttackItem) {
            ((IShieldAttackItem) activeStack.getItem()).onShieldAttack(activeStack, player);
            return;
        }

        // 默认攻击逻辑：攻击准星对准的实体
        Entity target = findTarget(player);

        if (target != null) {
            // 使用原版攻击方法，触发正常的挥剑动画和效果
            player.attackTargetEntityWithCurrentItem(target);
        } else {
            // 空挥
            player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    /**
     * 服务端射线检测目标实体
     */
    private Entity findTarget(EntityPlayerMP player) {
        double reach = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();

        Vec3d eyePos = player.getPositionEyes(1.0F);
        Vec3d lookVec = player.getLook(1.0F);
        Vec3d endPos = eyePos.add(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach);

        // 获取范围内所有实体
        List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(
                player,
                player.getEntityBoundingBox()
                        .expand(lookVec.x * reach, lookVec.y * reach, lookVec.z * reach)
                        .grow(1.0D));

        Entity closest = null;
        double closestDist = reach;

        for (Entity entity : entities) {
            if (!entity.canBeCollidedWith())
                continue;

            float padding = entity.getCollisionBorderSize();
            AxisAlignedBB aabb = entity.getEntityBoundingBox().grow(padding);
            RayTraceResult hit = aabb.calculateIntercept(eyePos, endPos);

            if (hit != null) {
                double dist = eyePos.distanceTo(hit.hitVec);
                if (dist < closestDist) {
                    closest = entity;
                    closestDist = dist;
                }
            }
        }

        return closest;
    }

    /**
     * 检查物品是否可格挡
     */
    private static boolean isBlockableItem(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        return stack.getItem() instanceof ItemShield
                || isSwordAndShield(stack.getItem());
    }
}
