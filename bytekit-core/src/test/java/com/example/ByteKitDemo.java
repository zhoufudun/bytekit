package com.example;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.bytekit.asm.interceptor.annotation.*;
import com.alibaba.deps.org.objectweb.asm.tree.ClassNode;
import com.alibaba.deps.org.objectweb.asm.tree.MethodNode;
import com.alibaba.bytekit.asm.MethodProcessor;
import com.alibaba.bytekit.asm.binding.Binding;
import com.alibaba.bytekit.asm.interceptor.InterceptorProcessor;
import com.alibaba.bytekit.asm.interceptor.parser.DefaultInterceptorClassParser;
import com.alibaba.bytekit.utils.AgentUtils;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.bytekit.utils.Decompiler;

/**
 * 
 * @author hengyunabc 2020-05-21
 *
 */
public class ByteKitDemo {

    public static class Sample {
        private int exceptionCount = 0;

        public String hello(String str, boolean exception) {
            if (exception) {
                exceptionCount++;
                throw new RuntimeException("test exception, str: " + str);
            }
            return "hello " + str;
        }
    }

    public static class PrintExceptionSuppressHandler {

        @ExceptionHandler(inline = true)
        public static void onSuppress(@Binding.Throwable Throwable e, @Binding.Class Object clazz) {
            System.out.println("exception handler: " + clazz);
            e.printStackTrace();
        }
    }

    public static class SampleInterceptor {

        @AtEnter(inline = true, suppress = RuntimeException.class, suppressHandler = PrintExceptionSuppressHandler.class)
        public static void atEnter(@Binding.This Object object, 
                @Binding.Class Object clazz,
                @Binding.Args Object[] args, 
                @Binding.MethodName String methodName,
                @Binding.MethodDesc String methodDesc) {
            System.out.println("atEnter, args[0]: " + args[0]);
        }

        @AtExit(inline = false)
        public static void atExit(@Binding.Return Object returnObject) {
            atExit2(returnObject);
        }

        @AtExceptionExit(inline = true, onException = RuntimeException.class)
        public static void atExceptionExit(@Binding.Throwable RuntimeException ex,
                @Binding.Field(name = "exceptionCount") int exceptionCount) {
            System.out.println("atExceptionExit, ex: " + ex.getMessage() + ", field exceptionCount: " + exceptionCount);
        }
        public static void atExit2(Object returnObject) {
            System.out.println("atExit2, returnObject: " + returnObject);
        }

        @AtExceptionExit(inline = true, onException = RuntimeException.class)

        @AtInvoke(name = "", inline = true, whenComplete = true,
                excludes = {"java.arthas.SpyAPI", "java.lang.Byte"
                , "java.lang.Boolean"
                , "java.lang.Short"
                , "java.lang.Character"
                , "java.lang.Integer"
                , "java.lang.Float"
                , "java.lang.Long"
                , "java.lang.Double"})
        public static void onInvoke(@Binding.This Object target, @Binding.Class Class<?> clazz,
                                    @Binding.InvokeInfo String invokeInfo) {
            System.out.println("atEnter, InvokeInfo: " + invokeInfo);
        }
    }

    public static void main(String[] args) throws Exception {
        AgentUtils.install();

        // 启动Sample，不断执行
        final Sample sample = new Sample();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; ++i) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                        String result = sample.hello("" + i, (i % 3) == 0);
                        System.out.println("call hello result: " + result);
                    } catch (Throwable e) {
                        // ignore
                        System.out.println("call hello exception: " + e.getMessage());
                    }
                }
            }
        });
        t.start();

        // 解析定义的 Interceptor类 和相关的注解
        DefaultInterceptorClassParser interceptorClassParser = new DefaultInterceptorClassParser();
        List<InterceptorProcessor> processors = interceptorClassParser.parse(SampleInterceptor.class);

        // 加载字节码: 通过Java asm 工具库将二进制的class字节码转换成的结构化对象
        ClassNode classNode = AsmUtils.loadClass(Sample.class);

        // 对加载到的字节码做增强处理
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("hello")) { // 只对hello方法进行增强
                MethodProcessor methodProcessor = new MethodProcessor(classNode, methodNode);
                for (InterceptorProcessor interceptor : processors) {
                    interceptor.process(methodProcessor);
                }
            }
        }

        // 获取增强后的字节码
        byte[] bytes = AsmUtils.toBytes(classNode);

        // 查看反编译结果
        System.out.println(Decompiler.decompile(bytes));

        // 等待，查看未增强里的输出结果
        TimeUnit.SECONDS.sleep(10);

        // 通过 reTransform 增强类
        AgentUtils.reTransform(Sample.class, bytes);
        System.in.read();
    }

}
