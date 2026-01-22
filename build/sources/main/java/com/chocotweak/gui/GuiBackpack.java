package com.chocotweak.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 背包存储GUI
 * 显示108格超大容量（12行×9列）
 */
@SideOnly(Side.CLIENT)
public class GuiBackpack extends GuiContainer {

    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(
            "textures/gui/container/generic_54.png");
    private static final int BACKPACK_ROWS = 12;

    public GuiBackpack(InventoryPlayer playerInv, ItemStack backpackStack) {
        super(new ContainerBackpack(playerInv, backpackStack));

        // GUI尺寸：背包12行 + 玩家物品栏
        this.xSize = 176;
        this.ySize = 18 + BACKPACK_ROWS * 18 + 14 + 76; // 背包 + 间隔 + 玩家物品栏
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "§6§l背包存储 §7(108格)";
        this.fontRenderer.drawString(title, 8, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;

        // 绘制背景 - 使用多个箱子纹理拼接
        this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);

        // 顶部边框
        this.drawTexturedModalRect(startX, startY, 0, 0, 176, 17);

        // 背包槽位行（每行18像素）- 12行
        for (int row = 0; row < BACKPACK_ROWS; row++) {
            int srcY = 17 + (row % 6) * 18; // 循环使用6行纹理
            this.drawTexturedModalRect(startX, startY + 17 + row * 18, 0, 17, 176, 18);
        }

        // 分隔线
        int sepY = startY + 17 + BACKPACK_ROWS * 18;
        this.drawTexturedModalRect(startX, sepY, 0, 125, 176, 14);

        // 玩家物品栏
        int playerInvY = sepY + 14;
        this.drawTexturedModalRect(startX, playerInvY, 0, 126, 176, 96);
    }
}
