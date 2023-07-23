package com.alibaba.bytekit.asm.interceptor.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.annotation.AtEnter.EnterInterceptorProcessorParser;
import com.alibaba.bytekit.asm.interceptor.parser.InterceptorProcessorParser;
import com.alibaba.bytekit.asm.location.EnterLocationMatcher;
import com.alibaba.bytekit.asm.location.LocationMatcher;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
// parserHander属性指定本注解的Parser类为EnterInterceptorProcessorParser.class
@InterceptorParserHander(parserHander = EnterInterceptorProcessorParser.class)
public @interface AtEnter {
    boolean inline() default true;

    Class<? extends Throwable> suppress() default None.class;

    Class<?> suppressHandler() default Void.class;

    class EnterInterceptorProcessorParser implements InterceptorProcessorParser {

        @Override
        public InterceptorProcessor parse(Method method, Annotation annotationOnMethod) {
            // 创建本注解的LocationMatcher实例：EnterLocationMatcher
            LocationMatcher locationMatcher = new EnterLocationMatcher();
            AtEnter atEnter = (AtEnter) annotationOnMethod;

            // 创建 InterceptorProcessor
            return InterceptorParserUtils.createInterceptorProcessor(method,
                    locationMatcher,
                    atEnter.inline(),
                    atEnter.suppress(),
                    atEnter.suppressHandler());

        }

    }
}
