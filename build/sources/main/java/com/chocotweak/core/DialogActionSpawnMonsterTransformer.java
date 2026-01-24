package com.chocotweak.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

/**
 * ASM类转换器 - 在DialogActionSpawnMonster.execute方法末尾注入驯服逻辑
 * 使用ASM而非Mixin避免影响DialogAction的静态初始化
 */
public class DialogActionSpawnMonsterTransformer implements IClassTransformer {

    private static final String TARGET_CLASS = "com.chocolate.chocolateQuest.quest.DialogActionSpawnMonster";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        if (!TARGET_CLASS.equals(transformedName) && !TARGET_CLASS.equals(name)) {
            return basicClass;
        }

        System.out.println("[ChocoTweak-ASM] Transforming DialogActionSpawnMonster...");

        try {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);

            // 找到execute方法
            MethodNode executeMethod = null;
            for (MethodNode method : classNode.methods) {
                if ("execute".equals(method.name)) {
                    executeMethod = method;
                    break;
                }
            }

            if (executeMethod == null) {
                System.err.println("[ChocoTweak-ASM] Could not find execute method!");
                return basicClass;
            }

            // 在每个 world.spawnEntity(entity) 调用之前插入驯服逻辑
            // 查找 INVOKEVIRTUAL net/minecraft/world/World.spawnEntity
            for (int i = 0; i < executeMethod.instructions.size(); i++) {
                AbstractInsnNode insn = executeMethod.instructions.get(i);
                
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    
                    // 检查是否是 spawnEntity 调用
                    if (("spawnEntity".equals(methodInsn.name) || "func_72838_d".equals(methodInsn.name)) 
                            && methodInsn.owner.contains("World")) {
                        
                        System.out.println("[ChocoTweak-ASM] Found spawnEntity call, injecting taming logic");
                        
                        // 在spawnEntity之前插入驯服调用
                        // 此时栈上是: World, Entity
                        // 我们需要: DUP2, 然后调用 TamingHelper.tryTame(World, Entity, Player)
                        
                        InsnList toInsert = new InsnList();
                        
                        // DUP entity (当前栈顶是entity)
                        toInsert.add(new InsnNode(Opcodes.DUP));
                        // 加载player参数 (第一个参数, index 1)
                        toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        // 调用静态助手方法
                        toInsert.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "com/chocotweak/core/TamingHelper",
                            "tryTameBeforeSpawn",
                            "(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/player/EntityPlayer;)V",
                            false
                        ));
                        
                        // 在spawnEntity调用之前插入
                        executeMethod.instructions.insertBefore(insn, toInsert);
                        
                        System.out.println("[ChocoTweak-ASM] Injected taming call before spawnEntity");
                        break; // 只处理一次
                    }
                }
            }

            // 写回字节码
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);

            System.out.println("[ChocoTweak-ASM] DialogActionSpawnMonster transformation complete!");
            return writer.toByteArray();

        } catch (Exception e) {
            System.err.println("[ChocoTweak-ASM] Error transforming DialogActionSpawnMonster: " + e.getMessage());
            e.printStackTrace();
            return basicClass;
        }
    }
}
