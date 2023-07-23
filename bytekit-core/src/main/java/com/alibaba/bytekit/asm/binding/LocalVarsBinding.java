package com.alibaba.bytekit.asm.binding;

import java.util.List;

import com.alibaba.deps.org.objectweb.asm.Type;
import com.alibaba.deps.org.objectweb.asm.tree.AbstractInsnNode;
import com.alibaba.deps.org.objectweb.asm.tree.InsnList;
import com.alibaba.deps.org.objectweb.asm.tree.LocalVariableNode;
import com.alibaba.bytekit.utils.AsmOpUtils;

/**
 * TODO 增加一个配置，是否包含 method args
 * @author hengyunabc
 *
 */
public class LocalVarsBinding extends Binding{

    @Override
    public void pushOntoStack(InsnList instructions, BindingContext bindingContext) {
        // 获取当前绑定位置的第一条指令
        AbstractInsnNode currentInsnNode = bindingContext.getLocation().getInsnNode();
        // 获取当前指令位置上有效的本地变量列表
        List<LocalVariableNode> results = AsmOpUtils
                .validVariables(bindingContext.getMethodProcessor().getMethodNode().localVariables, currentInsnNode);

        // 创建对象数组：new Object[ result.size() ]
        AsmOpUtils.push(instructions, results.size());
        AsmOpUtils.newArray(instructions, AsmOpUtils.OBJECT_TYPE);
        // 遍历本地变量列表，将每个变量添加到上面的Object数组中
        for (int i = 0; i < results.size(); ++i) {
            // dup : 复制栈顶最后一条指令，即上面的 object array ref
            AsmOpUtils.dup(instructions);
            // 写入的数组 index
            AsmOpUtils.push(instructions, i);
            // 读取本地变量，压入栈顶
            LocalVariableNode variableNode = results.get(i);
            AsmOpUtils.loadVar(instructions, Type.getType(variableNode.desc), variableNode.index);
            // 将primitive 类型转换为box对象
            AsmOpUtils.box(instructions, Type.getType(variableNode.desc));
            // 保存栈顶的变量到数组
            AsmOpUtils.arrayStore(instructions, AsmOpUtils.OBJECT_TYPE);
        }

    }

    @Override
    public Type getType(BindingContext bindingContext) {
        return AsmOpUtils.OBJECT_ARRAY_TYPE;
    }

}
