package com.chocotweak.gui;

import com.chocotweak.ChocoTweak;
import com.chocotweak.config.NpcDialogConfig;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 创造模式 GUI - 选择可驯服生物
 * 玩家可以从所有实体中选择一个，并用背包物品定义价格
 */
public class GuiSelectCreature extends GuiScreen {

    private GuiScreen parentScreen;
    private List<String> entityList = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private static final int VISIBLE_ENTRIES = 10;

    private GuiTextField searchField;
    private List<String> filteredList = new ArrayList<>();

    // 价格物品槽 (用背包里的物品)
    private List<ItemStack> costItems = new ArrayList<>();

    // 按钮 ID
    private static final int BTN_CONFIRM = 100;
    private static final int BTN_CANCEL = 101;
    private static final int BTN_SCROLL_UP = 102;
    private static final int BTN_SCROLL_DOWN = 103;
    private static final int BTN_ADD_COST = 104;
    private static final int BTN_CLEAR_COST = 105;

    public GuiSelectCreature(GuiScreen parent) {
        this.parentScreen = parent;
        loadEntityList();
    }

    private void loadEntityList() {
        entityList.clear();
        Set<ResourceLocation> keys = EntityList.getEntityNameList();
        for (ResourceLocation key : keys) {
            String name = key.toString();
            // 只显示可能可驯服的生物
            entityList.add(name);
        }
        entityList.sort(String::compareToIgnoreCase);
        filteredList.addAll(entityList);
    }

    @Override
    public void initGui() {
        int centerX = width / 2;
        int startY = 30;

        // 搜索框
        searchField = new GuiTextField(0, fontRenderer, centerX - 100, startY, 200, 20);
        searchField.setMaxStringLength(100);
        searchField.setFocused(true);

        // 确认/取消按钮
        buttonList.add(new GuiButton(BTN_CONFIRM, centerX - 105, height - 30, 100, 20,
                I18n.format("chocotweak.gui.confirm")));
        buttonList.add(new GuiButton(BTN_CANCEL, centerX + 5, height - 30, 100, 20,
                I18n.format("chocotweak.gui.cancel")));

        // 滚动按钮
        buttonList.add(new GuiButton(BTN_SCROLL_UP, centerX + 110, 60, 20, 20, "▲"));
        buttonList.add(new GuiButton(BTN_SCROLL_DOWN, centerX + 110, 60 + VISIBLE_ENTRIES * 15, 20, 20, "▼"));

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
                    saveCreatureEntry(filteredList.get(selectedIndex));
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
                // 添加玩家手持物品作为价格
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
        filterList();

        if (keyCode == 1) { // ESC
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);

        // 检查是否点击了列表项
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
                }
            }
        }
    }

    private void filterList() {
        String search = searchField.getText().toLowerCase();
        filteredList.clear();
        for (String entity : entityList) {
            if (entity.toLowerCase().contains(search)) {
                filteredList.add(entity);
            }
        }
        scrollOffset = 0;
        selectedIndex = -1;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // 标题
        String title = I18n.format("chocotweak.gui.select_creature");
        drawCenteredString(fontRenderer, title, width / 2, 10, 0xFFFFFF);

        // 搜索框
        searchField.drawTextBox();

        // 实体列表
        int listX = width / 2 - 100;
        int listY = 60;
        int entryHeight = 15;

        for (int i = 0; i < VISIBLE_ENTRIES && scrollOffset + i < filteredList.size(); i++) {
            int index = scrollOffset + i;
            String entityName = filteredList.get(index);
            int yPos = listY + i * entryHeight;

            // 高亮选中项
            if (index == selectedIndex) {
                drawRect(listX - 2, yPos - 1, listX + 202, yPos + entryHeight - 1, 0x80FFFFFF);
            }

            // 缩短显示名称
            String displayName = entityName;
            if (displayName.length() > 30) {
                displayName = displayName.substring(0, 27) + "...";
            }

            fontRenderer.drawString(displayName, listX, yPos, 0xFFFFFF);
        }

        // 显示已添加的价格物品
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

    private void saveCreatureEntry(String entityId) {
        NpcDialogConfig.CreatureEntry entry = new NpcDialogConfig.CreatureEntry();
        entry.entityId = entityId;
        entry.displayName = entityId.substring(entityId.indexOf(':') + 1);
        entry.costItems = new ArrayList<>();

        for (ItemStack stack : costItems) {
            NpcDialogConfig.CostItem cost = new NpcDialogConfig.CostItem();
            cost.itemId = stack.getItem().getRegistryName().toString();
            cost.count = stack.getCount();
            cost.metadata = stack.getMetadata();
            entry.costItems.add(cost);
        }

        NpcDialogConfig.getAvailableCreatures().add(entry);
        NpcDialogConfig.saveConfig();

        ChocoTweak.LOGGER.info("Added creature entry: {} with {} cost items", entityId, costItems.size());
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
