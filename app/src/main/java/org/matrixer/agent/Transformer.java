package org.matrixer.agent;

import java.io.IOException;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class Transformer implements ClassFileTransformer {

        String targetClassName;
        ClassLoader targetClassLoader;

    Transformer(String className, ClassLoader classLoader) {
        this.targetClassName = className;
        this.targetClassLoader = classLoader;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> cls, ProtectionDomain protectionDomain,
            byte[] classfileBuffer) {
        byte[] byteCode = classfileBuffer;
        String finalTargetClassName = this.targetClassName.replaceAll("\\.", "/");

        if (!className.equals(finalTargetClassName)) {
            return byteCode;
        }

        if (className.equals(finalTargetClassName) && loader.equals(targetClassLoader)) {
            System.out.println("[Agent] Transforming class " + className);
        }

        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cc = pool.get(targetClassName);
            for (var m : cc.getMethods()) {
                if (instrument(m)) {
                    System.out.println("Instrumented " + m.getLongName());
                }
            }
            byteCode = cc.toBytecode();
            cc.detach();
            return byteCode;
        } catch (CannotCompileException e) {
            System.err.println("Err Transformer.transform(): " + e.getReason());
            return null;
        } catch (NotFoundException | IOException e) {
            System.err.println("Err Transformer.transform(): " + e);
            return null;
        }
    }

    private boolean instrument(CtMethod method) throws NotFoundException, CannotCompileException, IOException {
            final var name = method.getLongName();
            if (name.startsWith("java.lang.Object")) {
                return false;
            }

            String regex = "^java.*|^org.junit.*|^jdk.*|^org.gradle.*|^com.sun.*";
            String callee = name;

            System.out.println("Found method: " + name);
            StringBuilder endBlock = new StringBuilder();
            endBlock.append("StackTraceElement[] elems = Thread.currentThread().getStackTrace();");
            endBlock.append("for (int i = 1; i < elems.length; i++) {");
            endBlock.append("   StackTraceElement elem = elems[i];");
            endBlock.append("   if (elem.getClassName().matches(\"" + regex + "\")) {");
            endBlock.append("       elem = elems[i-1];");
            endBlock.append("       String caller = elem.getClassName() + \":\" + elem.getMethodName();");
            endBlock.append("       System.out.println(\"Looks like " + callee + " was called by test \" + caller);");
            endBlock.append("       break;");
            endBlock.append("   }");
            endBlock.append("};");
            method.insertAfter(endBlock.toString());
            return true;
    }
}
