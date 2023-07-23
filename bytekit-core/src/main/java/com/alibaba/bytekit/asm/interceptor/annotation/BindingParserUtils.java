package com.alibaba.bytekit.asm.interceptor.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.binding.annotation.BindingParser;
import com.alibaba.bytekit.asm.binding.annotation.BindingParserHandler;
import com.alibaba.bytekit.utils.InstanceUtils;

public class BindingParserUtils {

    public static List<Binding> parseBindings(Method method) {
        // 从 parameter 里解析出来 @Binding
        List<Binding> bindings = new ArrayList<Binding>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < parameterAnnotations.length; ++parameterIndex) {
            Annotation[] annotationsOnParameter = parameterAnnotations[parameterIndex];
            for (int j = 0; j < annotationsOnParameter.length; ++j) {
                // 获取@Binding注解中参数中的所有注解，例如：com.alibaba.bytekit.asm.binding.Binding.This上的四个注解
                Annotation[] annotationsOnBinding = annotationsOnParameter[j].annotationType().getAnnotations();
                for (Annotation annotationOnBinding : annotationsOnBinding) {
                    if (BindingParserHandler.class.isAssignableFrom(annotationOnBinding.annotationType())) { // 获取指定的Binding解析器
                        BindingParserHandler bindingParserHandler = (BindingParserHandler) annotationOnBinding;
                        BindingParser bindingParser = InstanceUtils.newInstance(bindingParserHandler.parser());// 获取parser属性，实例化具体bind需要的解析器
                        Binding binding = bindingParser.parse(annotationsOnParameter[j]); // 举例：解析@com.alibaba.bytekit.asm.binding.Binding$Throwable(optional=false)
                        bindings.add(binding);
                    }
                }
            }
        }
        return bindings;
    }
}
