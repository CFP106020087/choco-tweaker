package com.chocotweak.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * ASM类转换器 - 在Awakements类加载时修改静态初始化块
 * 在awekements数组初始化后添加自定义觉醒
 */
public class AwakementsTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "com.chocolate.chocolateQuest.magic.Awakements";
    private static final String TARGET_CLASS_INTERNAL = "com/chocolate/chocolateQuest/magic/Awakements";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        // 检查是否是目标类
        if (!TARGET_CLASS.equals(transformedName) && !TARGET_CLASS.equals(name)) {
            return basicClass;
        }

        System.out.println("[ChocoTweak-ASM] Transforming Awakements class...");

        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // 找到clinit方法
            MethodNode clinit = null;
            for (MethodNode method : classNode.methods) {
                if ("<clinit>".equals(method.name)) {
                    clinit = method;
                    break;
                }
            }

            if (clinit == null) {
                System.err.println("[ChocoTweak-ASM] Could not find <clinit> in Awakements!");
                return basicClass;
            }

            // 在RETURN指令前插入我们的初始化代码
            InsnList toInsert = new InsnList();

            // 调用 AwakementsInitializer.initCustomAwakenings()
            toInsert.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "com/chocotweak/core/AwakementsInitializer",
                    "initCustomAwakenings",
                    "()V",
                    false));

            // 在每个RETURN前插入
            Iterator<AbstractInsnNode> iterator = clinit.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode insn = iterator.next();
                if (insn.getOpcode() == Opcodes.RETURN) {
                    clinit.instructions.insertBefore(insn, toInsert);
                    System.out.println("[ChocoTweak-ASM] Injected initCustomAwakenings() call before RETURN");
                    break; // clinit只有一个RETURN
                }
            }

            // 写回字节码
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            System.out.println("[ChocoTweak-ASM] Awakements transformation complete!");
            return writer.toByteArray();

        } catch (Exception e) {
            System.err.println("[ChocoTweak-ASM] Error transforming Awakements: " + e.getMessage());
            e.printStackTrace();
            return basicClass;
        }
    }
}
