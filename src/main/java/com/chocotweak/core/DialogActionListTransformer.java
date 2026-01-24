package com.chocotweak.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * ASM类转换器 - 重写 DialogActionList.toString() 以翻译动作名称
 * 替代 MixinDialogActionList，避免 Mixin 触发早期类加载
 */
public class DialogActionListTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "com.chocolate.chocolateQuest.quest.DialogActionList";
    private static final String INTERNAL_NAME = "com/chocolate/chocolateQuest/quest/DialogActionList";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        if (!TARGET_CLASS.equals(transformedName) && !TARGET_CLASS.equals(name)) {
            return basicClass;
        }

        System.out.println("[ChocoTweak-ASM] >>> Transforming DialogActionList.toString() for translation...");

        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // 找到 toString 方法
            MethodNode toStringMethod = null;
            for (MethodNode method : classNode.methods) {
                if ("toString".equals(method.name) && "()Ljava/lang/String;".equals(method.desc)) {
                    toStringMethod = method;
                    break;
                }
            }

            if (toStringMethod == null) {
                System.err.println("[ChocoTweak-ASM] Could not find toString() method in DialogActionList!");
                return basicClass;
            }

            // 完全替换 toString 方法体
            // 原来: return this.name;
            // 改为: return DialogActionTranslator.translateName(this.name);
            toStringMethod.instructions.clear();
            
            // 加载 this.name
            toStringMethod.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            toStringMethod.instructions.add(new FieldInsnNode(
                Opcodes.GETFIELD,
                INTERNAL_NAME,
                "name",
                "Ljava/lang/String;"
            ));
            
            // 调用 DialogActionTranslator.translateName(name)
            toStringMethod.instructions.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "com/chocotweak/util/DialogActionTranslator",
                "translateName",
                "(Ljava/lang/String;)Ljava/lang/String;",
                false
            ));
            
            // 返回
            toStringMethod.instructions.add(new InsnNode(Opcodes.ARETURN));

            // 写回字节码
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            System.out.println("[ChocoTweak-ASM] DialogActionList.toString() transformation complete!");
            return writer.toByteArray();

        } catch (Exception e) {
            System.err.println("[ChocoTweak-ASM] Error transforming DialogActionList: " + e.getMessage());
            e.printStackTrace();
            return basicClass;
        }
    }
}
