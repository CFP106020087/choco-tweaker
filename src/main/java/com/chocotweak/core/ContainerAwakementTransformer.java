package com.chocotweak.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * ASM类转换器 - 修复ContainerAwakement.enchantItem方法的觉醒ID查找问题
 * 
 * 在enchantItem方法开头插入调用，检测觉醒模式并用正确的ID查找
 */
public class ContainerAwakementTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "com.chocolate.chocolateQuest.gui.guinpc.ContainerAwakement";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        if (!TARGET_CLASS.equals(transformedName) && !TARGET_CLASS.equals(name)) {
            return basicClass;
        }

        System.out.println("[ChocoTweak-ASM] Transforming ContainerAwakement...");

        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // 找到enchantItem方法
            MethodNode enchantItem = null;
            for (MethodNode method : classNode.methods) {
                if ("enchantItem".equals(method.name)) {
                    enchantItem = method;
                    break;
                }
            }

            if (enchantItem == null) {
                System.err.println("[ChocoTweak-ASM] Could not find enchantItem method!");
                return basicClass;
            }

            // 在方法开头插入调用
            InsnList toInsert = new InsnList();

            // 加载this
            toInsert.add(new VarInsnNode(Opcodes.ALOAD, 0));
            // 加载enchantment参数
            toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
            // 调用静态处理方法
            toInsert.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "com/chocotweak/core/ContainerAwakementHelper",
                "handleEnchantItem",
                "(Ljava/lang/Object;Lnet/minecraft/enchantment/Enchantment;)Z",
                false
            ));
            // 如果返回true，直接return
            LabelNode continueLabel = new LabelNode();
            toInsert.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
            toInsert.add(new InsnNode(Opcodes.RETURN));
            toInsert.add(continueLabel);

            // 插入到方法开头
            enchantItem.instructions.insert(toInsert);

            System.out.println("[ChocoTweak-ASM] Injected handler into enchantItem");

            // 写回字节码
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            System.out.println("[ChocoTweak-ASM] ContainerAwakement transformation complete!");
            return writer.toByteArray();

        } catch (Exception e) {
            System.err.println("[ChocoTweak-ASM] Error transforming ContainerAwakement: " + e.getMessage());
            e.printStackTrace();
            return basicClass;
        }
    }
}
