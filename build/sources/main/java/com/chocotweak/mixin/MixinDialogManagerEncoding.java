package com.chocotweak.mixin;

import com.chocolate.chocolateQuest.quest.DialogOption;
import com.chocolate.chocolateQuest.utils.BDHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * 修复 CQ DialogManager 的编码问题
 * 
 * 原问题: saveText() 使用系统默认编码(Windows中文=GBK)写入
 * 但 loadTagsFromFile() 用UTF-8读取
 * 导致中文对话保存后再读取变成乱码
 * 
 * 修复: 强制使用UTF-8编码写入
 */
@Pseudo
@Mixin(targets = "com.chocolate.chocolateQuest.quest.DialogManager", remap = false)
public abstract class MixinDialogManagerEncoding {

    @Shadow
    static Map currentEntries;

    @Shadow
    protected static String getDefaultFileName(String fileName) {
        throw new AssertionError();
    }

    /**
     * @author ChocoTweak
     * @reason 修复中文对话保存编码问题 - 强制使用UTF-8
     */
    @Overwrite
    public static void saveText(DialogOption option) {
        try {
            File file = new File(BDHelper.getAppDir(), getDefaultFileName(option.folder));
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            // 使用反射获取或创建TextEntry，因为它是包内部类
            Object current = currentEntries.get(option.name);
            if (current == null) {
                // 创建新的TextEntry
                Class<?> textEntryClass = Class.forName("com.chocolate.chocolateQuest.quest.TextEntry");
                current = textEntryClass.getConstructor(String.class, String.class, String[].class)
                        .newInstance(option.name, option.prompt, option.text);
                currentEntries.put(option.name, current);
            } else {
                // 更新现有的TextEntry
                Field promptField = current.getClass().getField("prompt");
                Field textField = current.getClass().getField("text");
                promptField.set(current, option.prompt);
                textField.set(current, option.text);
            }

            // 修复: 使用UTF-8编码写入，而不是系统默认编码
            OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8);
            BufferedWriter stream = new BufferedWriter(writer);

            Iterator iterator = currentEntries.values().iterator();
            while (iterator.hasNext()) {
                Object entry = iterator.next();
                // 通过反射获取字段
                Field nameField = entry.getClass().getField("name");
                Field promptField = entry.getClass().getField("prompt");
                Field textField = entry.getClass().getField("text");

                String name = (String) nameField.get(entry);
                String prompt = (String) promptField.get(entry);
                String[] text = (String[]) textField.get(entry);

                stream.write("@name:" + name);
                stream.newLine();
                stream.write("@prompt:" + prompt);
                stream.newLine();

                if (text != null) {
                    for (String line : text) {
                        stream.write(line);
                        stream.newLine();
                    }
                }

                stream.write("#####");
                stream.newLine();
            }

            stream.close();

            com.chocotweak.ChocoTweak.LOGGER.debug(
                    "[ChocoTweak] Saved dialog with UTF-8 encoding: {}", option.name);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
