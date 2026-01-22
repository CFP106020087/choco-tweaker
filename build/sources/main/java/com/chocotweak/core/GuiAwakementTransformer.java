package com.chocotweak.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * ASM类转换器 - 在GuiAwakement.actionPerformed中捕获按钮ID
 * 在调用enchantItem之前设置pendingAwakementId
 */
public class GuiAwakementTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "com.chocolate.chocolateQuest.gui.guinpc.GuiAwakement";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        if (!TARGET_CLASS.equals(transformedName) && !TARGET_CLASS.equals(name)) {
            return basicClass;
        }

        System.out.println("[ChocoTweak-ASM] Transforming GuiAwakement...");

        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // 找到actionPerformed方法
            MethodNode actionPerformed = null;
            for (MethodNode method : classNode.methods) {
                if ("actionPerformed".equals(method.name)) {
                    actionPerformed = method;
                    break;
                }
            }

            if (actionPerformed == null) {
                System.err.println("[ChocoTweak-ASM] Could not find actionPerformed method!");
                return basicClass;
            }

            // 在方法开头插入：捕获button.id并设置到helper
            InsnList toInsert = new InsnList();

            // 加载button参数（参数1）
            toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
            // 获取button.id字段
            toInsert.add(new FieldInsnNode(
                Opcodes.GETFIELD,
                "net/minecraft/client/gui/GuiButton",
                "id",  // 可能是srg名或别的
                "I"
            ));
            // 存储到ContainerAwakementHelper.pendingAwakementId
            toInsert.add(new FieldInsnNode(
                Opcodes.PUTSTATIC,
                "com/chocotweak/core/ContainerAwakementHelper",
                "pendingAwakementId",
                "I"
            ));

            // 插入到方法开头
            actionPerformed.instructions.insert(toInsert);

            System.out.println("[ChocoTweak-ASM] Injected button.id capture into actionPerformed");

            // 写回字节码
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            System.out.println("[ChocoTweak-ASM] GuiAwakement transformation complete!");
            return writer.toByteArray();

        } catch (Exception e) {
            System.err.println("[ChocoTweak-ASM] Error transforming GuiAwakement: " + e.getMessage());
            e.printStackTrace();
            return basicClass;
        }
    }
}
