package com.chocotweak.gui.potion;

import com.chocotweak.ChocoTweak;
import com.chocotweak.magic.AwakementPotionCapacity;
import com.chocotweak.network.ChocoNetwork;
import com.chocotweak.network.PacketPotionInfusion;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * 药水灌注GUI - 客户端
 */
public class GuiPotionInfusion extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ChocoTweak.MODID, "textures/gui/potion_infusion.png");
    
    private ContainerPotionInfusion container;
    private GuiButton infuseButton;

    public GuiPotionInfusion(InventoryPlayer playerInventory, InventoryPotionInfusion infusionInventory) {
        super(new ContainerPotionInfusion(playerInventory, infusionInventory));
        this.container = (ContainerPotionInfusion) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        
        // 灌注按钮
        this.infuseButton = new GuiButton(0, guiLeft + 110, guiTop + 30, 50, 20, I18n.format("gui.chocotweak.infuse"));
        this.buttonList.add(this.infuseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // 更新按钮状态
        this.infuseButton.enabled = this.container.canInfuse();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0 && this.container.canInfuse()) {
            // 发送灌注请求到服务端
            ChocoNetwork.INSTANCE.sendToServer(new PacketPotionInfusion());
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // 标题
        String title = I18n.format("gui.chocotweak.potion_infusion");
        this.fontRenderer.drawString(title, (this.xSize - this.fontRenderer.getStringWidth(title)) / 2, 6, 0x404040);
        
        // 玩家背包标签
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
        
        // XP消耗提示
        String xpCost = I18n.format("gui.chocotweak.xp_cost", ContainerPotionInfusion.XP_COST);
        this.fontRenderer.drawString(xpCost, 110, 55, 0x80FF20);
        
        // 显示药水槽信息
        ItemStack weapon = this.container.getInfusionInventory().getStackInSlot(0);
        if (!weapon.isEmpty()) {
            int maxSlots = AwakementPotionCapacity.getPotionSlotsForWeapon(weapon);
            int current = getCurrentPotionCount(weapon);
            String slots = I18n.format("gui.chocotweak.potion_slots", current, maxSlots);
            this.fontRenderer.drawString(slots, 8, 55, 0x404040);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int guiLeft = (this.width - this.xSize) / 2;
        int guiTop = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize);
        
        // 绘制武器槽框
        this.drawTexturedModalRect(guiLeft + 25, guiTop + 34, 176, 0, 18, 18);
        
        // 绘制药水槽框
        this.drawTexturedModalRect(guiLeft + 79, guiTop + 34, 176, 0, 18, 18);
        
        // 绘制箭头
        this.drawTexturedModalRect(guiLeft + 50, guiTop + 35, 176, 18, 22, 16);
    }

    private int getCurrentPotionCount(ItemStack weapon) {
        if (!weapon.hasTagCompound()) return 0;
        if (!weapon.getTagCompound().hasKey("CustomPotionEffects", 9)) return 0;
        return weapon.getTagCompound().getTagList("CustomPotionEffects", 10).tagCount();
    }
}
