package com.alibaba.bytekit.asm.interceptor.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.annotation.InterceptorParserHander;
import com.alibaba.bytekit.utils.InstanceUtils;
import com.alibaba.bytekit.utils.ReflectionUtils;
import com.alibaba.bytekit.utils.ReflectionUtils.MethodCallback;

public class DefaultInterceptorClassParser implements InterceptorClassParser {

    @Override
    public List<InterceptorProcessor> parse(Class<?> clazz) {
        final List<InterceptorProcessor> result = new ArrayList<InterceptorProcessor>();

        MethodCallback methodCallback = new MethodCallback() {

            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                // 枚举拦截方法标记的所有Annatation注解
                for (Annotation onMethodAnnotation : method.getAnnotations()) {
                    for (Annotation onAnnotation : onMethodAnnotation.annotationType().getAnnotations()) {
                        // 找到InterceptorParserHander的子类，即@AtXxx注解
                        if (InterceptorParserHander.class.isAssignableFrom(onAnnotation.annotationType())) {
                            // 判断是否为静态方法
                            if (!Modifier.isStatic(method.getModifiers())) {
                                throw new IllegalArgumentException("method must be static. method: " + method);
                            }
                            // 找到@AtXxx注解指定的parserHander类并实例化
                            InterceptorParserHander handler = (InterceptorParserHander) onAnnotation;
                            InterceptorProcessorParser interceptorProcessorParser = InstanceUtils
                                    .newInstance(handler.parserHander());
                            // 调用InterceptorProcessorParser.parse()解析拦截方法生成InterceptorProcessor对象
                            InterceptorProcessor interceptorProcessor = interceptorProcessorParser.parse(method,
                                    onMethodAnnotation);
                            result.add(interceptorProcessor);
                        }
                    }
                }
            }

        };
        //枚举拦截器的所有方法
        ReflectionUtils.doWithMethods(clazz, methodCallback);

        return result;
    }

}
