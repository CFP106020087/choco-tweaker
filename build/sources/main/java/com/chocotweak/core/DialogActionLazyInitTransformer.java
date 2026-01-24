package com.chocotweak.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

/**
 * ASM类转换器 - 将DialogAction.actions静态数组改为懒加载
 * 防止静态初始化时加载子类导致的问题
 * 
 * 原始代码：
 * public static DialogActionList[] actions = new DialogActionList[] {...};
 * 
 * 转换后：
 * private static DialogActionList[] actions = null;
 * public static DialogActionList[] getActions() {
 * if (actions == null) { actions = new DialogActionList[] {...}; }
 * return actions;
 * }
 * 
 * 并替换所有 actions 字段访问为 getActions() 调用
 */
public class DialogActionLazyInitTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "com.chocolate.chocolateQuest.quest.DialogAction";
    private static final String INTERNAL_NAME = "com/chocolate/chocolateQuest/quest/DialogAction";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        // 打印所有 CQ 相关类的加载
        if (transformedName != null && transformedName.contains("chocolate")) {
            System.out.println("[ChocoTweak-ASM-LazyInit] *** Processing: " + transformedName);
        }

        // 对所有CQ类打印调试信息
        if (transformedName != null && transformedName.startsWith("com.chocolate.chocolateQuest.quest.Dialog")) {
            System.out.println("[ChocoTweak-ASM] DialogActionLazyInit checking: " + transformedName);
        }

        if (basicClass == null) {
            return null;
        }

        if (!TARGET_CLASS.equals(transformedName) && !TARGET_CLASS.equals(name)) {
            return basicClass;
        }

        System.out.println("[ChocoTweak-ASM] >>> Transforming DialogAction for lazy initialization...");

        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // 1. 找到静态初始化块 <clinit>
            MethodNode clinit = null;
            for (MethodNode method : classNode.methods) {
                if ("<clinit>".equals(method.name)) {
                    clinit = method;
                    break;
                }
            }

            if (clinit == null) {
                System.err.println("[ChocoTweak-ASM] No <clinit> found in DialogAction!");
                return basicClass;
            }

            // 2. 从 <clinit> 中移除 actions 数组初始化代码
            // 找到 PUTSTATIC com/chocolate/chocolateQuest/quest/DialogAction.actions 指令
            // 并保存初始化代码供后面使用
            InsnList actionsInitCode = new InsnList();
            boolean foundActionsInit = false;
            int startIndex = -1;
            int endIndex = -1;

            for (int i = 0; i < clinit.instructions.size(); i++) {
                AbstractInsnNode insn = clinit.instructions.get(i);
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                    if (fieldInsn.getOpcode() == Opcodes.PUTSTATIC &&
                            "actions".equals(fieldInsn.name) &&
                            fieldInsn.owner.equals(INTERNAL_NAME)) {

                        endIndex = i;
                        foundActionsInit = true;
                        System.out.println("[ChocoTweak-ASM] Found actions PUTSTATIC at index " + i);

                        // 向前查找数组创建的开始位置
                        // 查找 BIPUSH 或 SIPUSH (数组大小) 后跟 ANEWARRAY
                        for (int j = i - 1; j >= 0; j--) {
                            AbstractInsnNode prevInsn = clinit.instructions.get(j);
                            if (prevInsn instanceof IntInsnNode ||
                                    (prevInsn instanceof InsnNode && prevInsn.getOpcode() >= Opcodes.ICONST_0
                                            && prevInsn.getOpcode() <= Opcodes.ICONST_5)) {
                                // 检查下一条是否是 ANEWARRAY
                                if (j + 1 < clinit.instructions.size()) {
                                    AbstractInsnNode nextInsn = clinit.instructions.get(j + 1);
                                    if (nextInsn instanceof TypeInsnNode &&
                                            nextInsn.getOpcode() == Opcodes.ANEWARRAY) {
                                        startIndex = j;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }

            if (!foundActionsInit || startIndex < 0) {
                System.out.println(
                        "[ChocoTweak-ASM] Could not find actions initialization, skipping lazy init transform");
                return basicClass;
            }

            System.out.println("[ChocoTweak-ASM] Actions init code from " + startIndex + " to " + endIndex);

            // 3. 复制初始化代码，然后从clinit中删除
            for (int i = startIndex; i <= endIndex; i++) {
                actionsInitCode.add(clinit.instructions.get(i).clone(null));
            }

            // 删除原始初始化代码
            for (int i = endIndex; i >= startIndex; i--) {
                clinit.instructions.remove(clinit.instructions.get(i));
            }

            // 4. 创建 getActions() 方法
            MethodNode getActions = new MethodNode(
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNCHRONIZED,
                    "getActions",
                    "()[Lcom/chocolate/chocolateQuest/quest/DialogActionList;",
                    null,
                    null);

            // if (actions == null) { actions = ...; }
            LabelNode notNullLabel = new LabelNode();
            getActions.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, INTERNAL_NAME, "actions",
                    "[Lcom/chocolate/chocolateQuest/quest/DialogActionList;"));
            getActions.instructions.add(new JumpInsnNode(Opcodes.IFNONNULL, notNullLabel));

            // 插入复制的初始化代码
            getActions.instructions.add(actionsInitCode);

            getActions.instructions.add(notNullLabel);
            getActions.instructions.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
            getActions.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, INTERNAL_NAME, "actions",
                    "[Lcom/chocolate/chocolateQuest/quest/DialogActionList;"));
            getActions.instructions.add(new InsnNode(Opcodes.ARETURN));

            classNode.methods.add(getActions);
            System.out.println("[ChocoTweak-ASM] Created getActions() method");

            // 5. 替换所有对 actions 字段的 GETSTATIC 访问为 getActions() 调用
            for (MethodNode method : classNode.methods) {
                if ("getActions".equals(method.name) || "<clinit>".equals(method.name))
                    continue;

                for (int i = 0; i < method.instructions.size(); i++) {
                    AbstractInsnNode insn = method.instructions.get(i);
                    if (insn instanceof FieldInsnNode) {
                        FieldInsnNode fieldInsn = (FieldInsnNode) insn;
                        if (fieldInsn.getOpcode() == Opcodes.GETSTATIC &&
                                "actions".equals(fieldInsn.name) &&
                                fieldInsn.owner.equals(INTERNAL_NAME)) {
                            // 替换为 getActions() 调用
                            MethodInsnNode getActionsCall = new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    INTERNAL_NAME,
                                    "getActions",
                                    "()[Lcom/chocolate/chocolateQuest/quest/DialogActionList;",
                                    false);
                            method.instructions.set(insn, getActionsCall);
                        }
                    }
                }
            }

            // 写回字节码
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            System.out.println("[ChocoTweak-ASM] DialogAction lazy init transformation complete!");
            return writer.toByteArray();

        } catch (Exception e) {
            System.err.println("[ChocoTweak-ASM] Error transforming DialogAction: " + e.getMessage());
            e.printStackTrace();
            return basicClass;
        }
    }
}
