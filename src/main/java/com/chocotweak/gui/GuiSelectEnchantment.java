package com.chocotweak.gui;

import com.chocotweak.ChocoTweak;
import com.chocotweak.config.NpcDialogConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 创造模式 GUI - 选择附魔
 * 玩家可以从所有附魔中选择一个，并用背包物品定义价格
 */
public class GuiSelectEnchantment extends GuiScreen {

    private GuiScreen parentScreen;
    private List<EnchantmentInfo> enchantmentList = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private static final int VISIBLE_ENTRIES = 10;

    private GuiTextField searchField;
    private GuiTextField levelField;
    private List<EnchantmentInfo> filteredList = new ArrayList<>();

    // 价格物品槽
    private List<ItemStack> costItems = new ArrayList<>();

    // 按钮 ID
    private static final int BTN_CONFIRM = 100;
    private static final int BTN_CANCEL = 101;
    private static final int BTN_SCROLL_UP = 102;
    private static final int BTN_SCROLL_DOWN = 103;
    private static final int BTN_ADD_COST = 104;
    private static final int BTN_CLEAR_COST = 105;

    public GuiSelectEnchantment(GuiScreen parent) {
        this.parentScreen = parent;
        loadEnchantmentList();
    }

    private void loadEnchantmentList() {
        enchantmentList.clear();
        for (Enchantment enchant : Enchantment.REGISTRY) {
            if (enchant != null && enchant.getRegistryName() != null) {
                EnchantmentInfo info = new EnchantmentInfo();
                info.registryName = enchant.getRegistryName().toString();
                info.displayName = enchant.getTranslatedName(1);
                info.maxLevel = enchant.getMaxLevel();
                enchantmentList.add(info);
            }
        }
        enchantmentList.sort((a, b) -> a.displayName.compareToIgnoreCase(b.displayName));
        filteredList.addAll(enchantmentList);
    }

    @Override
    public void initGui() {
        int centerX = width / 2;
        int startY = 30;

        // 搜索框
        searchField = new GuiTextField(0, fontRenderer, centerX - 100, startY, 200, 20);
        searchField.setMaxStringLength(100);
        searchField.setFocused(true);

        // 等级输入框
        levelField = new GuiTextField(1, fontRenderer, centerX + 110, startY, 30, 20);
        levelField.setMaxStringLength(2);
        levelField.setText("1");

        // 确认/取消按钮
        buttonList.add(new GuiButton(BTN_CONFIRM, centerX - 105, height - 30, 100, 20,
                I18n.format("chocotweak.gui.confirm")));
        buttonList.add(new GuiButton(BTN_CANCEL, centerX + 5, height - 30, 100, 20,
                I18n.format("chocotweak.gui.cancel")));

        // 滚动按钮
        buttonList.add(new GuiButton(BTN_SCROLL_UP, centerX + 150, 60, 20, 20, "▲"));
        buttonList.add(new GuiButton(BTN_SCROLL_DOWN, centerX + 150, 60 + VISIBLE_ENTRIES * 15, 20, 20, "▼"));

        // 价格编辑按钮
        buttonList.add(new GuiButton(BTN_ADD_COST, centerX - 105, height - 55, 100, 20,
                I18n.format("chocotweak.gui.add_cost_item")));
        buttonList.add(new GuiButton(BTN_CLEAR_COST, centerX + 5, height - 55, 100, 20,
                I18n.format("chocotweak.gui.clear_cost")));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case BTN_CONFIRM:
                if (selectedIndex >= 0 && selectedIndex < filteredList.size()) {
                    int level = 1;
                    try {
                        level = Integer.parseInt(levelField.getText());
                    } catch (NumberFormatException e) {
                        level = 1;
                    }
                    saveEnchantmentEntry(filteredList.get(selectedIndex), level);
                }
                mc.displayGuiScreen(parentScreen);
                break;
            case BTN_CANCEL:
                mc.displayGuiScreen(parentScreen);
                break;
            case BTN_SCROLL_UP:
                if (scrollOffset > 0)
                    scrollOffset--;
                break;
            case BTN_SCROLL_DOWN:
                if (scrollOffset < filteredList.size() - VISIBLE_ENTRIES)
                    scrollOffset++;
                break;
            case BTN_ADD_COST:
                if (mc.player != null) {
                    ItemStack held = mc.player.getHeldItemMainhand();
                    if (!held.isEmpty()) {
                        costItems.add(held.copy());
                    }
                }
                break;
            case BTN_CLEAR_COST:
                costItems.clear();
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        searchField.textboxKeyTyped(typedChar, keyCode);
        levelField.textboxKeyTyped(typedChar, keyCode);
        filterList();

        if (keyCode == 1) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);
        levelField.mouseClicked(mouseX, mouseY, mouseButton);

        int listX = width / 2 - 100;
        int listY = 60;
        int listWidth = 200;
        int entryHeight = 15;

        if (mouseX >= listX && mouseX <= listX + listWidth) {
            int relativeY = mouseY - listY;
            if (relativeY >= 0 && relativeY < VISIBLE_ENTRIES * entryHeight) {
                int clickedIndex = scrollOffset + relativeY / entryHeight;
                if (clickedIndex < filteredList.size()) {
                    selectedIndex = clickedIndex;
                    // 设置最大等级
                    levelField.setText(String.valueOf(filteredList.get(clickedIndex).maxLevel));
                }
            }
        }
    }

    private void filterList() {
        String search = searchField.getText().toLowerCase();
        filteredList.clear();
        for (EnchantmentInfo info : enchantmentList) {
            if (info.displayName.toLowerCase().contains(search) ||
                    info.registryName.toLowerCase().contains(search)) {
                filteredList.add(info);
            }
        }
        scrollOffset = 0;
        selectedIndex = -1;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        String title = I18n.format("chocotweak.gui.select_enchantment");
        drawCenteredString(fontRenderer, title, width / 2, 10, 0xFFFFFF);

        searchField.drawTextBox();
        levelField.drawTextBox();

        // 等级标签
        fontRenderer.drawString(I18n.format("chocotweak.gui.level") + ":", width / 2 + 110, 20, 0xFFFFFF);

        // 附魔列表
        int listX = width / 2 - 100;
        int listY = 60;
        int entryHeight = 15;

        for (int i = 0; i < VISIBLE_ENTRIES && scrollOffset + i < filteredList.size(); i++) {
            int index = scrollOffset + i;
            EnchantmentInfo info = filteredList.get(index);
            int yPos = listY + i * entryHeight;

            if (index == selectedIndex) {
                drawRect(listX - 2, yPos - 1, listX + 202, yPos + entryHeight - 1, 0x80FFFFFF);
            }

            String displayName = info.displayName + " (Lv." + info.maxLevel + ")";
            if (displayName.length() > 35) {
                displayName = displayName.substring(0, 32) + "...";
            }

            fontRenderer.drawString(displayName, listX, yPos, 0xFFFFFF);
        }

        // 显示价格物品
        int costY = height - 80;
        String costLabel = I18n.format("chocotweak.gui.cost_items") + ":";
        fontRenderer.drawString(costLabel, width / 2 - 100, costY, 0xFFFF00);

        int costX = width / 2 - 100;
        for (int i = 0; i < costItems.size(); i++) {
            ItemStack stack = costItems.get(i);
            String itemText = stack.getCount() + "x " + stack.getDisplayName();
            fontRenderer.drawString(itemText, costX + (i % 3) * 70, costY + 12 + (i / 3) * 10, 0xAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void saveEnchantmentEntry(EnchantmentInfo info, int level) {
        NpcDialogConfig.EnchantmentEntry entry = new NpcDialogConfig.EnchantmentEntry();
        entry.enchantmentId = info.registryName;
        entry.level = Math.min(level, info.maxLevel);
        entry.costItems = new ArrayList<>();

        for (ItemStack stack : costItems) {
            NpcDialogConfig.CostItem cost = new NpcDialogConfig.CostItem();
            cost.itemId = stack.getItem().getRegistryName().toString();
            cost.count = stack.getCount();
            cost.metadata = stack.getMetadata();
            entry.costItems.add(cost);
        }

        NpcDialogConfig.getAvailableEnchantments().add(entry);
        NpcDialogConfig.saveConfig();

        ChocoTweak.LOGGER.info("Added enchantment entry: {} level {} with {} cost items",
                info.registryName, level, costItems.size());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static class EnchantmentInfo {
        String registryName;
        String displayName;
        int maxLevel;
    }
}
